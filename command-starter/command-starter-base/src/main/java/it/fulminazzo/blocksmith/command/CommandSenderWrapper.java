package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A wrapper for a general Command sender.
 *
 * @param <S> the actual type of the sender
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class CommandSenderWrapper<S> {
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull ApplicationHandle application;
    @Getter
    protected final @NotNull S actualSender;

    /**
     * Executes the given function in a synchronous context.
     * Useful in commands annotated with {@link it.fulminazzo.blocksmith.command.annotation.Async}.
     *
     * @param function the function to execute
     */
    public void sync(final @NotNull Consumer<S> function) {
        Scheduler.schedule(application, t -> function.accept(actualSender)).run();
    }

    /**
     * Checks if the actual sender extends the given Java class.
     *
     * @param type the type
     * @return <code>true</code> if it does
     */
    public final boolean extendsType(final @NotNull Class<?> type) {
        return Reflect.on(actualSender).extendsType(type);
    }

    /**
     * Gets the name of the sender.
     *
     * @return the name
     */
    public abstract @NotNull String getName();

    /**
     * Checks if the sender has the given permission.
     *
     * @param permissionInfo the permission info
     * @return <code>true</code> if they do
     */
    public final boolean hasPermission(final @NotNull PermissionInfo permissionInfo) {
        return permissionInfo.getGrant() == Permission.Grant.ALL || hasPermissionImpl(permissionInfo);
    }

    /**
     * Internal implementation for {@link #hasPermission(PermissionInfo)}.
     * <br>
     * Does NOT check if the permission is for {@link Permission.Grant#ALL}.
     *
     * @param permissionInfo the permission info
     * @return <code>true</code> if they have the permission
     */
    protected abstract boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo);

    /**
     * Checks if the internal sender are a player.
     *
     * @return <code>true</code> if they are
     */
    public abstract boolean isPlayer();

    /**
     * Gets a unique identifier for the wrapped sender.
     *
     * @return the id
     */
    public abstract @NotNull Object getId();

}
