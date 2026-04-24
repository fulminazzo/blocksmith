package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.message.receiver.Receiver;
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
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
    /**
     * The default name to identify the console in each platform.
     */
    public static final @NotNull String CONSOLE_COMMAND_NAME = "console";

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final @NotNull ApplicationHandle application;
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
     * Converts this sender to a {@link Receiver}.
     *
     * @return the receiver
     */
    public @NotNull Receiver receiver() {
        return ReceiverFactories.get(actualSender.getClass(), application).create(actualSender);
    }

    /**
     * Checks if the actual sender extends the given Java class.
     *
     * @param type the type
     * @return {@code true} if it does
     */
    public boolean extendsType(final @NotNull Type type) {
        return Reflect.on(actualSender).extendsType(type);
    }

    /**
     * Gets the unique identifier of the sender.
     * If the sender is <b>not</b> a player, it will use {@link #getName()}.
     *
     * @return the identifier
     */
    public final @NotNull Object getId() {
        return isPlayer() ? getIdImpl() : getName();
    }

    /**
     * Gets the name of the sender.
     * If the sender is <b>not</b> a player, it will return {@link #CONSOLE_COMMAND_NAME}.
     *
     * @return the name
     */
    public final @NotNull String getName() {
        return isPlayer() ? getNameImpl() : CONSOLE_COMMAND_NAME;
    }

    /**
     * Checks if the sender has the given permission.
     *
     * @param permissionInfo the permission info
     * @return {@code true} if they do
     */
    public boolean hasPermission(final @NotNull PermissionInfo permissionInfo) {
        return permissionInfo.getGrant() == Permission.Grant.ALL || hasPermissionImpl(permissionInfo);
    }

    /**
     * Gets the internal wrapped sender.
     *
     * @return the sender
     */
    public @NotNull S handle() {
        return actualSender;
    }

    /**
     * Checks if the internal sender is a player.
     *
     * @return {@code true} if they are
     */
    public abstract boolean isPlayer();

    /**
     * Gets a unique identifier for the wrapped sender.
     * <br>
     * Does <b>NOT</b> check if the wrapped sender is not a player.
     *
     * @return the id
     */
    protected abstract @NotNull Object getIdImpl();

    /**
     * Gets the actual name of the sender.
     * <br>
     * For internal use only.
     *
     * @return the name
     */
    protected abstract @NotNull String getNameImpl();

    /**
     * Internal implementation for {@link #hasPermission(PermissionInfo)}.
     * <br>
     * Does NOT check if the permission is for {@link Permission.Grant#ALL}.
     *
     * @param permissionInfo the permission info
     * @return {@code true} if they have the permission
     */
    protected abstract boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo);

}
