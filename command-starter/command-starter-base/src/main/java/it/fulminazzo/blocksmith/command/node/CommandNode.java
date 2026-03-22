package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Represents the node of a command, whether it is static or dynamic.
 */
@EqualsAndHashCode
@ToString
public abstract class CommandNode {
    @Getter
    private final @NotNull Set<CommandNode> children = new TreeSet<>(Comparator.comparing(CommandNode::getName));
    @Setter
    private @Nullable ExecutionInfo executionInfo;

    /**
     * Gets the first child.
     *
     * @return the first child (if present)
     */
    public @Nullable CommandNode getFirstChild() {
        return !children.isEmpty() ? children.iterator().next() : null;
    }

    /**
     * Attempts to fetch a child that matches the given input.
     *
     * @param token the token
     * @return the child (if found)
     */
    public @Nullable CommandNode getChild(final @NotNull String token) {
        for (CommandNode child : children)
            if (child instanceof LiteralNode && child.matches(token))
                return child;
        return children.stream().filter(c -> c.matches(token)).findFirst().orElse(null);
    }

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
                return c;
            }
        children.add(child);
        return child;
    }

    /**
     * If this node contains execution information, it represents the end of a command route.
     * As such, it should be executable with the parsed arguments so far.
     *
     * @return the execution information, if available
     */
    public Optional<ExecutionInfo> getExecutionInfo() {
        return Optional.ofNullable(executionInfo);
    }

    /**
     * Checks if the current node is executable.
     *
     * @return <code>true</code> if it is
     */
    public boolean isExecutable() {
        return executionInfo != null;
    }

    /**
     * Merges the given node data with the current one.
     *
     * @param node the node
     * @return this node
     */
    public @NotNull CommandNode merge(final @NotNull CommandNode node) {
        node.getChildren().forEach(this::addChild);
        if (executionInfo == null) node.getExecutionInfo().ifPresent(this::setExecutionInfo);
        return this;
    }

    /**
     * Walks the children to construct the best command route that matches the given input.
     *
     * @param context the context
     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
     */
    public void execute(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        validateInput(context);
        if (context.advanceCursor().isDone()) {
            if (isExecutable()) internalExecute(context);
            else throw new CommandExecutionException("error.not-enough-arguments");
        } else {
            CommandNode child = getChild(context.getCurrent());
            if (child == null) {
                if (isExecutable()) internalExecute(context);
                else throw new CommandExecutionException("error.command-not-found");
            } else child.execute(context);
        }
    }

    private void internalExecute(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        try {
            ExecutionInfo executionInfo = getExecutionInfo().orElseThrow();
            Method method = executionInfo.getMethod();
            LinkedList<Object> arguments = context.getArguments();
            if (arguments.size() != method.getParameterCount())
                arguments.addFirst(context.getCommandSender());
            method.invoke(executionInfo.getExecutor(), arguments.toArray());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new CommandExecutionException("error.internal-error", e);
        }
    }

    /**
     * Validates the current input of the context.
     *
     * @param context the context
     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
     */
    protected abstract void validateInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;

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
