package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A Command sender wrapper for Bungeecord.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
final class BungeeCommandSenderWrapper extends CommandSenderWrapper<CommandSender> {

    /**
     * Instantiates a new Bungee command sender wrapper.
     *
     * @param application  the application
     * @param actualSender the actual sender
     */
    public BungeeCommandSenderWrapper(final @NotNull ApplicationHandle application,
                                      final @NotNull CommandSender actualSender) {
        super(application, actualSender);
    }

    @Override
    public boolean isPlayer() {
        return actualSender instanceof ProxiedPlayer;
    }

    @Override
    protected @NotNull Object getIdImpl() {
        return ((ProxiedPlayer) actualSender).getUniqueId();
    }

    @Override
    protected @NotNull String getNameImpl() {
        return actualSender.getName();
    }

    @Override
    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
        return actualSender.hasPermission(permissionInfo.getPermission());
    }

}
