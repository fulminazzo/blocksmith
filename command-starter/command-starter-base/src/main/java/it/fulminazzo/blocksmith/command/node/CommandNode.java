package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.TabCompletable;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.cooldown.CooldownManager;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the node of a command, whether it is static or dynamic.
 */
@EqualsAndHashCode
@ToString
public abstract class CommandNode implements TabCompletable {
    @Getter
    private final @NotNull Set<CommandNode> children = new TreeSet<>(Comparator.comparing(CommandNode::getName));
    @Setter
    private @Nullable ExecutionInfo executionInfo;
    private @Nullable CooldownManager<Object> cooldownManager;

    /**
     * Sets an execution cooldown for the current node.
     * <br>
     * <b>WARNING</b>: only works if {@link #executionInfo} is defined.
     *
     * @param cooldown the cooldown
     */
    public void setCooldown(final @NotNull Duration cooldown) {
        cooldownManager = new CooldownManager<>(cooldown);
    }

    /**
     * Gets the first child.
     *
     * @return the child (if found)
     */
    public @Nullable CommandNode getFirstChild() {
        return !children.isEmpty() ? children.iterator().next() : null;
    }

    /**
     * Gets the first child that is a {@link ArgumentNode} and is optional.
     *
     * @return the child (if found)
     */
    public @Nullable ArgumentNode<?> getFirstOptionalArgumentNode() {
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
     * Gets all the {@link ArgumentNode} children that are greedy.
     *
     * @return the children
     */
    public @NotNull List<ArgumentNode<?>> getGreedyChildren() {
        return children.stream()
                .filter(c -> c instanceof ArgumentNode)
                .map(c -> (ArgumentNode<?>) c)
                .filter(ArgumentNode::isGreedy)
                .collect(Collectors.toList());
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
        validateExecuteInput(context);
        handleRemainingInput(context);
    }

    /**
     * Handles the remaining input from the context.
     *
     * @param context the context
     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
     */
    void handleRemainingInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        if (context.advanceCursor().isDone()) {
            if (isExecutable()) internalExecute(context);
            else {
                ArgumentNode<?> optional = getFirstOptionalArgumentNode();
                if (optional != null) {
                    context.addParsedArgument(optional.getDefaultValue(context));
                    optional.handleRemainingInput(context);
                } else throw new CommandExecutionException("error.not-enough-arguments");
            }
        } else {
            String current = context.getCurrent();
            CommandNode child = getChild(current);
            if (child == null) {
                if (isExecutable()) internalExecute(context);
                else throw new CommandExecutionException("error.command-not-found")
                        .arguments(Placeholder.of("argument", current));
            } else child.execute(context);
        }
    }

    private void internalExecute(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        ExecutionInfo executionInfo = getExecutionInfo().orElseThrow();
        if (cooldownManager != null) {
            Object id = context.getCommandSender().getId();
            if (cooldownManager.isOnCooldown(id))
                throw new CommandExecutionException("error.command-on-cooldown");
            else cooldownManager.putOnCooldown(id);
        }
        Method method = executionInfo.getMethod();
        try {
            LinkedList<Object> arguments = context.getArguments();
            if (arguments.size() != method.getParameterCount()) {
                CommandSenderWrapper sender = context.getCommandSender();
                Class<?> parameterType = method.getParameterTypes()[0];
                if (parameterType.equals(CommandSenderWrapper.class))
                    arguments.addFirst(sender);
                else if (sender.extendsType(parameterType)) arguments.addFirst(sender.getActualSender());
                else throw new CommandExecutionException(sender.isPlayer()
                            ? "error.player-cannot-execute"
                            : "error.console-cannot-execute"
                    );
            }
            method.invoke(executionInfo.getExecutor(), arguments.toArray());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new CommandExecutionException("error.internal-error", cause)
                    .arguments(Placeholder.of("message", cause.getMessage()));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Method %s#%s should be declared public",
                    method.getDeclaringClass().getCanonicalName(),
                    ReflectionUtils.methodToString(method)
            ));
        }
    }

    /**
     * Returns all the tab completions based on the current context of execution.
     *
     * @param context the context
     * @return the completions
     */
    public @NotNull List<String> tabComplete(final @NotNull CommandExecutionContext context) {
        if (context.isDone()) return Collections.emptyList();
        else {
            try {
                validateTabCompleteInput(context);
                if (context.isLast() || context.advanceCursor().isLast())
                    return filterCompletions(context, getChildren().stream()
                            .map(c -> c.getCompletions(context))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList())
                    );
                else {
                    CommandNode child = getChild(context.getCurrent());
                    if (child == null) return Collections.emptyList();
                    else {
                        List<ArgumentNode<?>> greedyChildren = getGreedyChildren();
                        if (greedyChildren.contains(child))
                            return filterCompletions(context, child.getCompletions(context));
                        else return child.tabComplete(context);
                    }
                }
            } catch (CommandExecutionException e) {
                return Collections.emptyList();
            }
        }
    }

    private @NotNull List<String> filterCompletions(final @NotNull CommandExecutionContext context,
                                                    final @NotNull List<String> completions) {
        List<String> finalCompletions = completions.stream()
                .filter(c -> c.toLowerCase().startsWith(context.getCurrent().toLowerCase()))
                .collect(Collectors.toList());
        if (finalCompletions.isEmpty())
            return completions.stream()
                    .filter(c -> c.startsWith("<"))
                    .collect(Collectors.toList());
        else return finalCompletions;
    }

    /**
     * Validates the current input of the context during execution.
     *
     * @param context the context
     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
     */
    protected abstract void validateExecuteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;

    /**
     * Validates the current input of the context during tab completion.
     *
     * @param context the context
     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
     */
    protected abstract void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;

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
