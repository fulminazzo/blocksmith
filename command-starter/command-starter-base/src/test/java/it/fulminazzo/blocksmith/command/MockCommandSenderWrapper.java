package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * A Command sender wrapper for testing purposes.
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class MockCommandSenderWrapper extends CommandSenderWrapper<CommandSender> {

    /**
     * Instantiates a new Mock command sender wrapper.
     *
     * @param actualSender the actual sender
     */
    public MockCommandSenderWrapper(final @NotNull CommandSender actualSender) {
        this(new MockApplicationHandle(), actualSender);
    }

    /**
     * Instantiates a new Mock command sender wrapper.
     *
     * @param application  the application
     * @param actualSender the actual sender
     */
    public MockCommandSenderWrapper(final @NotNull ApplicationHandle application,
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
