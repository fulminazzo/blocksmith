package it.fulminazzo.blocksmith.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
final class VelocityCommandSenderWrapper extends CommandSenderWrapper {
    private final @NotNull CommandSource actualSender;

    @Override
    public @NotNull String getName() {
        return isPlayer() ? ((Player) actualSender).getUsername() : "console";
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
