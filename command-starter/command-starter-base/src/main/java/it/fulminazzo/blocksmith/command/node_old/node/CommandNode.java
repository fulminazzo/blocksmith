package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
//import it.fulminazzo.blocksmith.command.TabCompletable;
//import it.fulminazzo.blocksmith.command.annotation.Permission;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.cooldown.StaticCooldownManager;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import it.fulminazzo.blocksmith.message.argument.Time;
//import lombok.*;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Represents the node of a command, whether it is static or dynamic.
// */
//@EqualsAndHashCode
//@ToString
//public abstract class CommandNode implements TabCompletable {
//
//    @EqualsAndHashCode.Exclude
//    @ToString.Exclude
//    private @Nullable AsyncManager asyncManager;
//
//    /**
//     * Gets the timeout to execute the command asynchronously.
//     *
//     * @return the timeout (if given)
//     */
//    public @Nullable Duration getAsyncTimeout() {
//        return asyncManager == null ? null : asyncManager.getTimeout();
//    }
//
//    /**
//     * Sets the command to run asynchronously.
//     * <br>
//     * <b>WARNING</b>: only works if {@link #executionInfo} is defined.
//     *
//     * @param timeout the timeout
//     */
//    public void setAsync(final @Nullable Duration timeout) {
//        if (timeout == null) asyncManager = null;
//        else if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be positive or zero");
//        else asyncManager = new AsyncManager(timeout);
//    }
//
//    /**
//     * Gets the first literal node (starting from this node) that represents the actual subcommand.
//     *
//     * @return the node (if found)
//     */
//    public @Nullable LiteralNode getCommandLiteral() {
//        if (this instanceof LiteralNode) return (LiteralNode) this;
//        else if (parent == null) return null;
//        else return parent.getCommandLiteral();
//    }
//
//    /**
//     * Gets the first child.
//     *
//     * @return the child (if found)
//     */
//    public @Nullable CommandNode getFirstChild() {
//        return !children.isEmpty() ? children.iterator().next() : null;
//    }
//
//    /**
//     * Gets the first child that is a {@link ArgumentNode} and is optional.
//     *
//     * @return the child (if found)
//     */
//    public @Nullable ArgumentNode<?> getFirstOptionalArgumentNode() {
//        for (CommandNode child : children)
//            if (child instanceof ArgumentNode<?>) {
//                ArgumentNode<?> argumentNode = (ArgumentNode<?>) child;
//                if (argumentNode.isOptional())
//                    return argumentNode;
//            }
//        return null;
//    }
//
//    /**
//     * Gets all the {@link ArgumentNode} children that are greedy.
//     *
//     * @return the children
//     */
//    public @NotNull List<ArgumentNode<?>> getGreedyChildren() {
//        return children.stream()
//                .filter(c -> c instanceof ArgumentNode)
//                .map(c -> (ArgumentNode<?>) c)
//                .filter(ArgumentNode::isGreedy)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Gets the permission to execute this node.
//     *
//     * @return the permission, if available
//     */
//    public @NotNull Optional<PermissionInfo> getPermission() {
//        if (this instanceof LiteralNode) return ((LiteralNode) this).getCommandInfo().map(CommandInfo::getPermission);
//        else if (parent == null) return Optional.empty();
//        else return parent.getPermission();
//    }
//
//    /**
//     * Walks the children to construct the best command route that matches the given input.
//     *
//     * @param context the context
//     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
//     */
//    public void execute(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        processInput(context);
//        handleRemainingInput(context);
//    }
//
//    /**
//     * Handles the remaining input from the context.
//     *
//     * @param context the context
//     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
//     */
//    void handleRemainingInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (context.advanceCursor().isDone()) {
//            if (isExecutable()) executeOrAwaitConfirmation(context);
//            else {
//                ArgumentNode<?> optional = getFirstOptionalArgumentNode();
//                if (optional != null) {
//                    context.addParsedArgument(optional.getDefaultValue(context));
//                    optional.handleRemainingInput(context);
//                } else throw new CommandExecutionException("error.not-enough-arguments");
//            }
//        } else {
//            String current = context.getCurrent();
//            CommandNode child = getChild(current);
//            if (child == null) {
//                if (isExecutable()) executeOrAwaitConfirmation(context);
//                else throw new CommandExecutionException("error.command-not-found")
//                        .arguments(Placeholder.of("argument", current));
//            } else child.execute(context);
//        }
//    }
//
//    private void executeOrAwaitConfirmation(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        LiteralNode literalNode = getCommandLiteral();
//        if (literalNode != null && literalNode.requiresConfirmation()) {
//            Duration confirmationTimeout = literalNode.getConfirmationTimeout();
//            literalNode.getPendingActionManager().register(
//                    context.getCommandSender().getId(),
//                    confirmationTimeout,
//                    () -> {
//                        try {
//                            internalExecute(context);
//                        } catch (CommandExecutionException e) {
//                            context.getRegistry().handleCommandExecutionException(e, context);
//                        }
//                    }
//            );
//            throw new CommandExecutionException("general.await-confirmation")
//                    .arguments(Time.of("time", confirmationTimeout.toMillis()));
//        } else internalExecute(context);
//    }
//
//    private void internalExecute(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (cooldownManager != null) {
//            CommandSenderWrapper<?> sender = context.getCommandSender();
//            PermissionInfo cooldownPermission = getCooldownBypassPermission(getPermission().orElseThrow());
//            if (!sender.hasPermission(cooldownPermission)) {
//                Object id = sender.getId();
//                if (cooldownManager.isOnCooldown(id)) {
//                    long time = cooldownManager.getRemainingCooldown(id);
//                    throw new CommandExecutionException("error.command-on-cooldown")
//                            .arguments(Time.of("cooldown", time));
//                } else cooldownManager.putOnCooldown(id);
//            }
//        }
//        final ExecutionInfo executionInfo = getExecutionInfo().orElseThrow();
//        if (asyncManager != null) asyncManager.execute(executionInfo, context);
//        else executionInfo.invoke(context);
//    }
//
//    /**
//     * Returns all the tab completions based on the current context of execution.
//     *
//     * @param context the context
//     * @return the completions
//     */
//    public @NotNull List<String> tabComplete(final @NotNull CommandExecutionContext context) {
//        if (context.isDone()) return Collections.emptyList();
//        else {
//            try {
//                validateTabCompleteInput(context);
//                if (context.isLast() || context.advanceCursor().isLast()) {
//                    List<String> completions = getChildren().stream()
//                            .map(c -> c.getCompletions(context))
//                            .flatMap(Collection::stream)
//                            .collect(Collectors.toList());
//                    return filterCompletions(context, completions);
//                } else {
//                    CommandNode child = getChild(context.getCurrent());
//                    if (child == null) return Collections.emptyList();
//                    else {
//                        List<ArgumentNode<?>> greedyChildren = getGreedyChildren();
//                        if (greedyChildren.contains(child))
//                            return filterCompletions(context, child.getCompletions(context));
//                        else return child.tabComplete(context);
//                    }
//                }
//            } catch (CommandExecutionException e) {
//                return Collections.emptyList();
//            }
//        }
//    }
//
//    /**
//     * Removes any completion not starting with &lt; and not matching the last input.
//     *
//     * @param context     the context
//     * @param completions the completions
//     * @return the completions
//     */
//    @NotNull List<String> filterCompletions(final @NotNull CommandExecutionContext context,
//                                                    final @NotNull List<String> completions) {
//        List<String> finalCompletions = completions.stream()
//                .filter(c -> c.toLowerCase().startsWith(context.getCurrent().toLowerCase()))
//                .collect(Collectors.toList());
//        if (finalCompletions.isEmpty())
//            return completions.stream()
//                    .filter(c -> c.startsWith("<"))
//                    .collect(Collectors.toList());
//        else return finalCompletions;
//    }
//
//    /**
//     * Validates and processes the current input of the context during execution.
//     * After execution, if an argument was parsed, it will be available through {@link CommandExecutionContext#getArguments()}.
//     *
//     * @param context the context
//     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
//     */
//    protected abstract void processInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;
//
//    /**
//     * Validates the current input of the context during tab completion.
//     *
//     * @param context the context
//     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
//     */
//    protected abstract void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;
//
//    /**
//     * Gets the associated permission to bypass the cooldown.
//     *
//     * @param permission the permission
//     * @return the cooldown bypass permission
//     */
//    public static @NotNull PermissionInfo getCooldownBypassPermission(final @NotNull PermissionInfo permission) {
//        return new PermissionInfo(
//                permission.getPrefix(),
//                "bypass.cooldown." + permission.getActualPermission(),
//                Permission.Grant.NONE // leave the actual check to platforms
//        );
//    }
//
//}
