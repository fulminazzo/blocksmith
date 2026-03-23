package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
final class BungeeCommandSenderWrapper extends CommandSenderWrapper {
    private final @NotNull CommandSender actualSender;

    @Override
    public @NotNull String getName() {
        return actualSender.getName();
    }

    @Override
    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
        return actualSender.hasPermission(permissionInfo.getPermission());
    }

    @Override
    public boolean isPlayer() {
        return actualSender instanceof ProxiedPlayer;
    }

}
