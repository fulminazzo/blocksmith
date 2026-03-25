package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class MockCommandSenderWrapper extends CommandSenderWrapper {
    @Getter
    private final @NotNull CommandSender actualSender;

    @Override
    public @NotNull String getName() {
        return actualSender.getName();
    }

    @Override
    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
        if (permissionInfo.getGrant() == Permission.Grant.OP && actualSender.isOp()) return true;
        return actualSender.hasPermission(permissionInfo.getPermission());
    }

    @Override
    public boolean isPlayer() {
        return actualSender instanceof Player;
    }

    @Override
    public @NotNull Object getId() {
        return getName();
    }

}
