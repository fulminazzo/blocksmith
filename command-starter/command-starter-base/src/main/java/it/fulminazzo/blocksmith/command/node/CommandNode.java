package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a node in a command tree.
 * A command node can have a parent, multiple children, and an optional executor.
 */
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class CommandNode {
    @Getter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable CommandNode parent;
    @Getter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    final @NotNull Set<CommandNode> children = new TreeSet<>(Comparator.comparing(CommandNode::getName));

    @Setter
    @Nullable ExecutionHandler executor;

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
     * If this node is executable, it means we reached the end of a command route.
     * As such, it should be executable with the parsed arguments so far.
     *
     * @return the actual executor of the command, if available
     */
    public @NotNull Optional<ExecutionHandler> getExecutor() {
        return Optional.ofNullable(executor);
    }

    /**
     * Checks if the node is executable.
     *
     * @return <code>true</code> if it is
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
            if (child.getClass().equals(c.getClass()) && c.getName().equals(child.getName())) {
                c.merge(child);
                c.parent = this;
                return c;
            }
        children.add(child);
        child.parent = this;
        return child;
    }

    /**
     * Gets the child that is a {@link ArgumentNode} and is optional.
     *
     * @return the first child that matches these criteria or <code>null</code> if not found
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
     * @return the child or <code>null</code> if not found
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
     * @return the first child or <code>null</code> if there are no children
     */
    public @Nullable CommandNode getFirstChild() {
        return !children.isEmpty() ? children.iterator().next() : null;
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
     * @return <code>true</code> if it does
     */
    public abstract boolean matches(final @NotNull String token);

    /**
     * Gets the name of this node.
     *
     * @return the name
     */
    public abstract @NotNull String getName();

}
