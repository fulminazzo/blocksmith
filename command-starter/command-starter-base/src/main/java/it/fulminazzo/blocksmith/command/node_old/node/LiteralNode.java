package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.action.PendingActionManager;
//import it.fulminazzo.blocksmith.command.annotation.Confirm;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import lombok.*;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.time.Duration;
//import java.util.*;
//
///**
// * A {@link CommandNode} to represent general literals.
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//@ToString(callSuper = true)
//public final class LiteralNode extends CommandNode {
//
//    @Getter(AccessLevel.NONE)
//    private @Nullable Confirm confirmAnnotation;
//
//    @EqualsAndHashCode.Exclude
//    @ToString.Exclude
//    private final @NotNull PendingActionManager<Object> pendingActionManager = new PendingActionManager<>();
//
//    /**
//     * Creates a clone of the current node, with the given literals.
//     *
//     * @param literals the new literals
//     * @return the clone
//     */
//    public @NotNull LiteralNode clone(final String @NotNull ... literals) {
//        LiteralNode clone = new LiteralNode(literals);
//        getChildren().forEach(clone::addChild);
//        getExecutionInfo().ifPresent(clone::setExecutionInfo);
//        getCommandInfo().ifPresent(clone::setCommandInfo);
//        clone.setCooldown(getCooldown());
//        clone.setConfirmationInfo(confirmAnnotation);
//        clone.setAsync(getAsyncTimeout());
//        return clone;
//    }
//
//    /**
//     * Checks if the execution of the current node tree requires confirmation.
//     *
//     * @return <code>true</code> if it does
//     */
//    public boolean requiresConfirmation() {
//        return confirmAnnotation != null;
//    }
//
//    /**
//     * Enables or disables confirmation for this node.
//     */
//    public void setConfirmationInfo(final @Nullable Confirm confirmAnnotation) {
//        this.confirmAnnotation = confirmAnnotation;
//    }
//
//    /**
//     * Gets the confirmation timeout (if set).
//     *
//     * @return the confirmation timeout
//     */
//    public @NotNull Duration getConfirmationTimeout() {
//        Confirm annotation = getNonNullConfirmationAnnotation();
//        return Duration.of(annotation.timeout(), annotation.unit().toChronoUnit());
//    }
//
//    private @NotNull Confirm getNonNullConfirmationAnnotation() {
//        return Objects.requireNonNull(this.confirmAnnotation, "Confirmation annotation not set");
//    }
//
//    /**
//     * Gets the word required for confirmation.
//     *
//     * @return the word
//     */
//    public @NotNull String getConfirmWord() {
//        return getNonNullConfirmationAnnotation().confirmWord();
//    }
//
//    /**
//     * Gets the cancellation word.
//     *
//     * @return the word
//     */
//    public @NotNull String getCancelWord() {
//        return getNonNullConfirmationAnnotation().cancelWord();
//    }
//
//    /**
//     * Checks if the sender of the current context has permission to execute this node.
//     *
//     * @param context the context
//     * @return <code>true</code> if they do
//     */
//    boolean hasPermission(final @NotNull CommandExecutionContext context) {
//        return getCommandInfo()
//                .map(CommandInfo::getPermission)
//                .map(p -> context.getCommandSender().hasPermission(p))
//                .orElse(true);
//    }
//
//    @Override
//    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//        return hasPermission(context) ? new ArrayList<>(aliases) : Collections.emptyList();
//    }
//
//    @Override
//    protected void processInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        checkPermission(context);
//        if (requiresConfirmation() && !context.isLast()) {
//            String argument = context.peek();
//
//            final Object id = context.getCommandSender().getId();
//            final PendingActionManager.Result result;
//            final String message;
//            if (argument.equalsIgnoreCase(getConfirmWord())) {
//                result = pendingActionManager.execute(id);
//                message = "";
//            } else if (argument.equalsIgnoreCase(getCancelWord())) {
//                result = pendingActionManager.cancel(id);
//                message = "success.pending-action-cancelled";
//            } else {
//                result = null;
//                message = null;
//            }
//
//            if (result == PendingActionManager.Result.NOT_FOUND)
//                throw new CommandExecutionException("error.no-pending-action");
//            else if (result == PendingActionManager.Result.EXPIRED)
//                throw new CommandExecutionException("error.pending-action-expired");
//            else if (result != null) throw new CommandExecutionException(message);
//        }
//    }
//
//    @Override
//    public @NotNull List<String> tabComplete(final @NotNull CommandExecutionContext context) {
//        List<String> completions = new ArrayList<>(super.tabComplete(context));
//        if (context.isLast() || context.advanceCursor().isLast()) {
//            Object id = context.getCommandSender().getId();
//            if (pendingActionManager.isPending(id))
//                completions.addAll(Arrays.asList(getConfirmWord(), getCancelWord()));
//        }
//        return filterCompletions(context, completions);
//    }
//
//    @Override
//    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        checkPermission(context);
//    }
//
//    private void checkPermission(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (!hasPermission(context))
//            throw new CommandExecutionException("error.no-permission")
//                    .arguments(Placeholder.of("permission", getCommandInfo().orElseThrow().getPermission().getPermission()));
//    }
//
//}
