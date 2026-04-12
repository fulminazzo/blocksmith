//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import it.fulminazzo.blocksmith.ApplicationHandle;//import it.fulminazzo.blocksmith.command.annotation.Permission;
//import it.fulminazzo.blocksmith.command.node.PermissionInfo;
//import it.fulminazzo.blocksmith.message.Messenger;
//import lombok.*;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.jspecify.annotations.NonNull;
//import org.slf4j.Logger;
//
///**
// * A Command sender wrapper for testing purposes.
// */
//@Slf4j
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
//public final class MockCommandSenderWrapper extends CommandSenderWrapper<CommandSender> {
//
//    /**
//     * Instantiates a new Mock command sender wrapper.
//     *
//     * @param actualSender the actual sender
//     */
//    public MockCommandSenderWrapper(final @NonNull CommandSender actualSender) {
//        this(new ApplicationHandle() {
//
//            @Override
//            public @NotNull Messenger getMessenger() {
//                return new Messenger(log);
//            }
//
//            @Override
//            public @NotNull Logger getLog() {
//                return log;
//            }
//
//            @Override
//            public @NotNull Object getServer() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public @NotNull String getName() {
//                return "blocksmith";
//            }
//
//        }, actualSender);
//    }
//
//    /**
//     * Instantiates a new Mock command sender wrapper.
//     *
//     * @param application  the application
//     * @param actualSender the actual sender
//     */
//    public MockCommandSenderWrapper(final @NotNull ApplicationHandle application,
//                                    final @NonNull CommandSender actualSender) {
//        super(application, actualSender);
//    }
//
//    @Override
//    public @NotNull String getName() {
//        return actualSender.getName();
//    }
//
//    @Override
//    protected boolean hasPermissionImpl(final @NotNull PermissionInfo permissionInfo) {
//        if (permissionInfo.getGrant() == Permission.Grant.OP && actualSender.isOp()) return true;
//        return actualSender.hasPermission(permissionInfo.getPermission());
//    }
//
//    @Override
//    public boolean isPlayer() {
//        return actualSender instanceof Player;
//    }
//
//    @Override
//    public @NotNull Object getId() {
//        return getName();
//    }
//
//}
