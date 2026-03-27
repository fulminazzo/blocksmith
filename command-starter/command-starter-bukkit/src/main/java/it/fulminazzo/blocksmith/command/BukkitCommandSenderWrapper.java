package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import lombok.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
final class BukkitCommandSenderWrapper extends CommandSenderWrapper<CommandSender> {
    private final @NotNull CommandSender actualSender;

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
