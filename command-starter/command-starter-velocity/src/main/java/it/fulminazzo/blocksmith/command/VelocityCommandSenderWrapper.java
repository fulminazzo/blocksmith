package it.fulminazzo.blocksmith.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A Command sender wrapper for Velocity.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
final class VelocityCommandSenderWrapper extends CommandSenderWrapper<CommandSource> {

    /**
     * Instantiates a new Velocity command sender wrapper.
     *
     * @param application  the application
     * @param actualSender the actual sender
     */
    public VelocityCommandSenderWrapper(final @NotNull ApplicationHandle application,
                                        final @NotNull CommandSource actualSender) {
        super(application, actualSender);
    }

    @Override
    protected @NotNull String getNameImpl() {
        return ((Player) actualSender).getUsername();
    }

    @Override
    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
        return actualSender.hasPermission(permissionInfo.getPermission());
    }

    @Override
    public boolean isPlayer() {
        return actualSender instanceof Player;
    }

    @Override
    public @NotNull Object getId() {
        return isPlayer() ? ((Player) actualSender).getUniqueId() : getName();
    }

}
