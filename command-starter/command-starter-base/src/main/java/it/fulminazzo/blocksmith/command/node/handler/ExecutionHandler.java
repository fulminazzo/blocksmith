package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor;
import it.fulminazzo.blocksmith.message.argument.Time;
import it.fulminazzo.blocksmith.structure.cooldown.FixedCooldownManager;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * Base {@link IExecutionHandler} implementation.
 */
@EqualsAndHashCode
@ToString
public final class ExecutionHandler implements IExecutionHandler {
    private final @NotNull CommandExecutor executor;

    private @Nullable FixedCooldownManager<Object> cooldownManager;

    private @Nullable AsyncManager asyncManager;

    /**
     * Instantiates a new Execution handler.
     *
     * @param executor the instance executing the command method
     * @param method   the method containing the command logic
     */
    public ExecutionHandler(final @NotNull Object executor, final @NotNull Method method) {
        this.executor = new CommandExecutor(executor, method);
    }

    /**
     * Sets a time to wait before executing the command again.
     *
     * @param cooldown the cooldown ({@code null} to disable)
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionHandler setCooldown(final @Nullable Duration cooldown) {
        if (cooldown == null) cooldownManager = null;
        else cooldownManager = new FixedCooldownManager<>(cooldown);
        return this;
    }

    /**
     * Flags this handler to run the {@link CommandExecutor} asynchronously.
     *
     * @param executorService the service to run asynchronous commands on
     * @param timeout         the timeout
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionHandler setAsync(final @NotNull ExecutorService executorService,
                                              final @NotNull Duration timeout) {
        if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be positive or zero");
        else asyncManager = new AsyncManager(executorService, timeout);
        return this;
    }

    /**
     * Unsets the asynchronous flag.
     *
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionHandler unsetAsync() {
        asyncManager = null;
        return this;
    }

    /**
     * Gets the actual executor of the command method.
     *
     * @return the executor
     */
    public <T> T getExecutor() {
        return executor.getExecutor().get();
    }

    /**
     * Gets the method containing the command logic.
     *
     * @return the method
     */
    public @NotNull Method getMethod() {
        return executor.getMethod();
    }

    @Override
    public void execute(final @NotNull LiteralNode commandNode,
                        final @NotNull CommandExecutionVisitor executionVisitor) throws CommandExecutionException {
        if (cooldownManager != null) {
            CommandSenderWrapper<?> sender = executionVisitor.getCommandSender();
            PermissionInfo cooldownPermission = getCooldownBypassPermission(commandNode.getCommandInfo().getPermission());
            if (!sender.hasPermission(cooldownPermission)) {
                Object id = sender.getId();
                if (cooldownManager.isOnCooldown(id)) {
                    long time = cooldownManager.getRemaining(id);
                    throw new CommandExecutionException(CommandMessages.COMMAND_ON_COOLDOWN)
                            .arguments(Time.of("cooldown", time));
                } else cooldownManager.put(id);
            }
        }
        if (asyncManager != null) asyncManager.execute(executor, executionVisitor);
        else executor.execute(executionVisitor);
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
