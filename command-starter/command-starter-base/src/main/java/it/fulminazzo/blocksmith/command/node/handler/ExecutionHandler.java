package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.ExecutionContext;
import it.fulminazzo.blocksmith.message.argument.Time;
import it.fulminazzo.blocksmith.structure.cooldown.FixedCooldownManager;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Handler to actually execute the command upon successful arguments validation.
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class ExecutionHandler {
    private final @NotNull CommandExecutor executor;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private @Nullable FixedCooldownManager<Object> cooldownManager;

    /**
     * Executes the actual command logic.
     *
     * @param commandNode the literal node containing information about the executing command
     * @param context the context of execution
     * @throws CommandExecutionException in case of any errors
     */
    public void execute(final @NotNull LiteralNode commandNode,
                        final @NotNull ExecutionContext context) throws CommandExecutionException {
        if (cooldownManager != null) {
            CommandSenderWrapper<?> sender = context.getCommandSender();
            PermissionInfo cooldownPermission = getCooldownBypassPermission(commandNode.getCommandInfo()
                    .map(CommandInfo::getPermission)
                    .orElseThrow(() -> new IllegalStateException("Could not get permission from node: " + commandNode))
            );
            if (!sender.hasPermission(cooldownPermission)) {
                Object id = sender.getId();
                if (cooldownManager.isOnCooldown(id)) {
                    long time = cooldownManager.getRemaining(id);
                    throw new CommandExecutionException("error.command-on-cooldown")
                            .arguments(Time.of("cooldown", time));
                } else cooldownManager.put(id);
            }
        }
        //TODO: async
        executor.execute(context);
    }

    /**
     * Sets a time to wait before executing the command again.
     *
     * @param cooldown the cooldown (<code>null</code> to disable)
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionHandler setCooldown(final @Nullable Duration cooldown) {
        if (cooldown == null) cooldownManager = null;
        else cooldownManager = new FixedCooldownManager<>(cooldown);
        return this;
    }

    /**
     * Gets the associated permission to bypass the cooldown.
     *
     * @param permission the permission
     * @return the cooldown bypass permission
     */
    public static @NotNull PermissionInfo getCooldownBypassPermission(final @NotNull PermissionInfo permission) {
        return new PermissionInfo(
                permission.getPrefix(),
                "bypass.cooldown." + permission.getActualPermission(),
                Permission.Grant.NONE // leave the actual check to platforms
        );
    }

}
