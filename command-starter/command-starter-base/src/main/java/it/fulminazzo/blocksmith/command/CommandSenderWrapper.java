package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for a general Command sender.
 */
public abstract class CommandSenderWrapper {

    /**
     * Checks if the actual sender extends the given Java class.
     *
     * @param type the type
     * @return <code>true</code> if it does
     */
    public final boolean extendsType(final @NotNull Class<?> type) {
        return getActualSender().getClass().isAssignableFrom(type);
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
     * Gets the actual sender instance.
     *
     * @return the actual sender
     */
    public abstract @NotNull Object getActualSender();

}
