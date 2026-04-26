package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.TabCompletable;
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler;
import it.fulminazzo.blocksmith.command.node.handler.IExecutionHandler;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.command.visitor.usage.UsageVisitor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a node in a command tree.
 * A command node can have a parent, multiple children, and an optional executor.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class CommandNode implements TabCompletable {
    @Getter
    @Nullable CommandNode parent;
    @Getter
    final @NotNull Set<CommandNode> children = Collections.synchronizedSet(new TreeSet<>(Comparator.comparing(CommandNode::getName)));

    @Setter
    @Nullable IExecutionHandler executor;

    /**
     * Merges the given node data with the current one.
     *
     * @param node the node
     * @return this node
     */
    public @NotNull CommandNode merge(final @NotNull CommandNode node) {
        node.getChildren().forEach(this::addChild);
        if (executor == null) node.getExecutor().ifPresent(this::setExecutor);
        return this;
    }

    /*
     * GETTERS
     */

    /**
     * Gets the help command name.
     *
     * @return the name
     */
    public @NotNull String getHelpCommandName() {
        return getHelpCommand().getName();
    }

    /**
     * Gets the {@link CommandNode} representing the help node.
     *
     * @return the help node
     */
    public @NotNull CommandNode getHelpCommand() {
        LiteralNode commandNode = Objects.requireNonNull(getCommandNode(), "Could not find command node in " + this);
        return commandNode.getChildren().stream()
                .filter(c -> c instanceof HelpNode)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find help node in " + commandNode));
    }

    /**
     * Gets the usage of the current node.
     *
     * @return the usage
     */
    public @NotNull String getUsage() {
        return UsageVisitor.generateUsage(this);
    }

    /**
     * Gets the node that carries information about the command (description, permission...).
     * <br>
     * If this node is a {@link LiteralNode}, itself will be returned.
     * Otherwise, the {@link #parent} will be checked.
     * <br>
     * For example, in the command {@code /clan member <player> promote <rank>},
     * this method would return:
     * <ul>
     *     <li>the node representing {@code promote} for {@code promote} and {@code <rank>};</li>
     *     <li>the node representing {@code member} for {@code member} and {@code <player>};</li>
     *     <li>the node representing {@code clan} for {@code clan}.</li>
     * </ul>
     *
     * @return the command node or {@code null} if not found (this should never happen on well-formed nodes)
     */
    public @Nullable LiteralNode getCommandNode() {
        if (this instanceof LiteralNode) return (LiteralNode) this;
        else return parent == null ? null : parent.getCommandNode();
    }

    /**
     * If this node is executable, it means we reached the end of a command route.
     * As such, it should be executable with the parsed arguments so far.
     *
     * @return the actual executor of the command, if available
     */
    public @NotNull Optional<IExecutionHandler> getExecutor() {
        return Optional.ofNullable(executor);
    }

    /**
     * Checks if the node is executable.
     *
     * @return {@code true} if it is
     */
    public boolean isExecutable() {
        return executor != null;
    }

    /*
     * CHILDREN
     */

    /**
     * Adds a new child to this node.
     *
     * @param child the child
     * @return the added child (if it was already present, it might be returned updated)
     */
    public @NotNull CommandNode addChild(final @NotNull CommandNode child) {
        for (CommandNode c : children)
            if (child.getClass().equals(c.getClass()) && (
                    c.getName().equals(child.getName()) || c instanceof HelpNode
            )) {
                c.merge(child);
                c.parent = this;
                return c;
            }
        children.add(child);
        child.parent = this;
        return child;
    }

    /**
     * Gets all the children of type {@link LiteralNode}.
     *
     * @return the nodes
     */
    public @NotNull Collection<LiteralNode> getSubcommands() {
        return children.stream()
                .filter(n -> n instanceof LiteralNode)
                .map(n -> (LiteralNode) n)
                .collect(Collectors.toList());
    }

    /**
     * Gets the child that is a {@link ArgumentNode} and is optional.
     *
     * @return the first child that matches these criteria or {@code null} if not found
     */
    public @Nullable ArgumentNode<?> getOptionalArgument() {
        for (CommandNode child : children)
            if (child instanceof ArgumentNode<?>) {
                ArgumentNode<?> argumentNode = (ArgumentNode<?>) child;
                if (argumentNode.isOptional())
                    return argumentNode;
            }
        return null;
    }

    /**
     * Attempts to fetch a child that matches the given input.
     * Will prioritize {@link LiteralNode}s.
     *
     * @param token the token
     * @return the child or {@code null} if not found
     */
    public @Nullable CommandNode getChild(final @NotNull String token) {
        for (CommandNode child : children)
            if (child instanceof LiteralNode && child.matches(token))
                return child;
        return children.stream().filter(c -> c.matches(token)).findFirst().orElse(null);
    }

    /**
     * Gets the first child of this node.
     *
     * @return the first child or {@code null} if there are no children
     */
    public @Nullable CommandNode getFirstChild() {
        return !children.isEmpty() ? children.iterator().next() : null;
    }

    @Override
    public int hashCode() {
        List<Object> hashObjects = new ArrayList<>();
        hashObjects.add(getClass());
        hashObjects.add(getName());
        if (executor instanceof ExecutionHandler) hashObjects.add(executor);
        return Objects.hash(hashObjects.toArray());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CommandNode) {
            CommandNode node = (CommandNode) obj;
            if (!node.getClass().equals(getClass()) && node.getName().equals(getName())) return false;
            if (executor instanceof ExecutionHandler) return executor.equals(node.executor);
            else return true;
        } else return false;
    }

    @Override
    public @NotNull String toString() {
        String base = String.format("%s(", CommandNode.class.getSimpleName());
        if (executor instanceof ExecutionHandler) base += "executor=" + executor;
        return base + ")";
    }

    /*
     * ABSTRACT
     */

    /**
     * Inspects this node with the given visitor.
     *
     * @param <T>     the type of the result
     * @param <X>     the type of the exception to throw in case of errors during visits
     * @param visitor the visitor
     * @return the result
     * @throws X the exception thrown in case of visit errors
     */
    public abstract <T, X extends Exception> T accept(final @NotNull Visitor<T, X> visitor) throws X;

    /**
     * Checks if the node matches with the given token.
     *
     * @param token the token
     * @return {@code true} if it does
     */
    public abstract boolean matches(final @NotNull String token);

    /**
     * Gets the name of this node.
     *
     * @return the name
     */
    public abstract @NotNull String getName();

}
