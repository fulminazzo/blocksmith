package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.action.PendingActionManager;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * A {@link CommandNode} to represent general literals.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class LiteralNode extends CommandNode {
    private final @NotNull String name;
    private final @NotNull Set<String> aliases;
    private @Nullable CommandInfo commandInfo;

    private @Nullable Duration confirmationTimeout;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull PendingActionManager<Object> pendingActionManager = new PendingActionManager<>();

    /**
     * Instantiates a new Literal node.
     *
     * @param literals the literals
     */
    public LiteralNode(final String @NotNull ... literals) {
        if (literals.length == 0) throw new IllegalArgumentException("At least one literal must be provided");
        this.name = literals[0].trim().toLowerCase();
        this.aliases = new HashSet<>();
        for (String literal : literals) this.aliases.add(literal.trim().toLowerCase());
    }

    /**
     * Creates a clone of the current node, with the given literals.
     *
     * @param literals the new literals
     * @return the clone
     */
    public @NotNull LiteralNode clone(final String @NotNull ... literals) {
        LiteralNode clone = new LiteralNode(literals);
        getChildren().forEach(clone::addChild);
        getExecutionInfo().ifPresent(clone::setExecutionInfo);
        getCommandInfo().ifPresent(clone::setCommandInfo);
        clone.setCooldown(getCooldown());
        clone.setConfirmationTimeout(getConfirmationTimeout());
        clone.setAsync(getAsyncTimeout());
        return clone;
    }

    /**
     * Checks if the execution of the current node tree requires confirmation.
     *
     * @return <code>true</code> if it does
     */
    public boolean requiresConfirmation() {
        return confirmationTimeout != null;
    }

    /**
     * Enables or disables confirmation for this node.
     */
    public void setConfirmationTimeout(final @Nullable Duration confirmationTimeout) {
        this.confirmationTimeout = confirmationTimeout;
    }

    /**
     * If this literal represents the final command (or subcommand) of a command route,
     * its command information will be available.
     *
     * @return the command info, if available
     */
    public @NotNull Optional<CommandInfo> getCommandInfo() {
        return Optional.ofNullable(commandInfo);
    }

    /**
     * Checks if the sender of the current context has permission to execute this node.
     *
     * @param context the context
     * @return <code>true</code> if they do
     */
    boolean hasPermission(final @NotNull CommandExecutionContext context) {
        return getCommandInfo()
                .map(CommandInfo::getPermission)
                .map(p -> context.getCommandSender().hasPermission(p))
                .orElse(true);
    }

    @Override
    public @NotNull LiteralNode merge(final @NotNull CommandNode node) {
        if (node instanceof LiteralNode) {
            LiteralNode literalNode = (LiteralNode) node;
            aliases.addAll(literalNode.aliases);
            literalNode.getCommandInfo().ifPresent(i -> {
                if (commandInfo == null) setCommandInfo(i);
                else commandInfo.merge(i);
            });
        }
        return (LiteralNode) super.merge(node);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return hasPermission(context) ? new ArrayList<>(aliases) : Collections.emptyList();
    }

    @Override
    protected void processInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        checkPermission(context);
        if (requiresConfirmation() && !context.isLast()) {
            String argument = context.peek();

            final Object id = context.getCommandSender().getId();
            final PendingActionManager.Result result;
            final String message;
            if (argument.equalsIgnoreCase("confirm")) {
                result = pendingActionManager.execute(id);
                message = "";
            } else if (argument.equalsIgnoreCase("cancel")) {
                result = pendingActionManager.cancel(id);
                message = "success.pending-action-cancelled";
            } else {
                result = null;
                message = null;
            }

            if (result == PendingActionManager.Result.NOT_FOUND)
                throw new CommandExecutionException("error.no-pending-action");
            else if (result == PendingActionManager.Result.EXPIRED)
                throw new CommandExecutionException("error.pending-action-expired");
            else if (result != null) throw new CommandExecutionException(message);
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandExecutionContext context) {
        List<String> completions = new ArrayList<>(super.tabComplete(context));
        if (context.isLast() || context.advanceCursor().isLast())
            completions.addAll(Arrays.asList("confirm", "cancel"));
        return filterCompletions(context, completions);
    }

    @Override
    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        checkPermission(context);
    }

    private void checkPermission(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        if (!hasPermission(context))
            throw new CommandExecutionException("error.no-permission")
                    .arguments(Placeholder.of("permission", getCommandInfo().orElseThrow().getPermission().getPermission()));
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return aliases.contains(token.trim().toLowerCase());
    }

}
