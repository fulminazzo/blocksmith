package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A Command sender wrapper for Bukkit.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
final class BukkitCommandSenderWrapper extends CommandSenderWrapper<CommandSender> {

    /**
     * Instantiates a new Bukkit command sender wrapper.
     *
     * @param application  the application
     * @param actualSender the actual sender
     */
    public BukkitCommandSenderWrapper(final @NotNull ApplicationHandle application,
                                      final @NotNull CommandSender actualSender) {
        super(application, actualSender);
    }

    @Override
    public @NotNull String getName() {
        return actualSender.getName();
    }

    @Override
    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
        if (permissionInfo.getGrant() == Permission.Grant.OP && actualSender.isOp()) return true;
        else return actualSender.hasPermission(permissionInfo.getPermission());
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
