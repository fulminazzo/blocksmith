//TODO: update
//package it.fulminazzo.blocksmith.command;
//
//import it.fulminazzo.blocksmith.ApplicationHandle;
//import it.fulminazzo.blocksmith.command.annotation.Permission.Grant;
//import it.fulminazzo.blocksmith.command.node.CommandNode;
//import it.fulminazzo.blocksmith.command.node.LiteralNode;
//import it.fulminazzo.blocksmith.command.node.PermissionInfo;
//import org.bukkit.Server;
//import org.bukkit.permissions.Permission;
//import org.bukkit.permissions.PermissionDefault;
//import org.bukkit.plugin.PluginManager;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
///**
// * Keeps track of all the permissions registered by {@link CommandRegistry}.
// */
//final class BukkitPermissionRegistry {
//    private final @NotNull PluginManager pluginManager;
//    private final @NotNull Map<String, Permission> previousPermissions = new ConcurrentHashMap<>();
//
//    /**
//     * Instantiates a new Bukkit permission registry.
//     *
//     * @param application the application that is initializing the registry
//     */
//    public BukkitPermissionRegistry(final @NotNull ApplicationHandle application) {
//        this.pluginManager = ((Server) application.getServer()).getPluginManager();
//    }
//
//    /**
//     * Registers a new Bukkit Permission for the given node.
//     *
//     * @param node the command node
//     * @return the registered permission
//     */
//    public @NotNull Permission registerPermission(final @NotNull LiteralNode node) {
//        PermissionInfo permissionInfo = node.getCommandInfo().orElseThrow().getPermission();
//        Set<String> childrenPermissions = getChildrenPermissions(node);
//        Permission permission = new BukkitPermission(permissionInfo, childrenPermissions);
//        Permission previous = pluginManager.getPermission(permission.getName());
//        if (!(previous instanceof BukkitPermission)) {
//            if (previous != null) {
//                pluginManager.removePermission(previous);
//                previousPermissions.put(permission.getName(), previous);
//            }
//            pluginManager.addPermission(permission);
//        }
//        return permission;
//    }
//
//    /**
//     * Gets the children permissions of the given node.
//     *
//     * @param node the node
//     * @return the permissions
//     */
//    @NotNull Set<String> getChildrenPermissions(final @NotNull CommandNode node) {
//        Set<String> permissions = new HashSet<>();
//        for (CommandNode child : node.getChildren()) {
//            if (child instanceof LiteralNode) {
//                Permission permission = registerPermission((LiteralNode) child);
//                permissions.add(permission.getName());
//            }
//            permissions.addAll(getChildrenPermissions(child));
//        }
//        return permissions;
//    }
//
//    /**
//     * Unregisters the given permission.
//     *
//     * @param permission the permission
//     */
//    public void unregisterPermission(final @Nullable String permission) {
//        if (permission != null) unregisterPermission(pluginManager.getPermission(permission));
//    }
//
//    /**
//     * Unregisters the given permission.
//     *
//     * @param permission the permission
//     */
//    public void unregisterPermission(final @Nullable Permission permission) {
//        if (permission == null) return;
//        String name = permission.getName();
//        Permission perm = pluginManager.getPermission(name);
//        if (!(perm instanceof BukkitPermission)) return;
//        pluginManager.removePermission(perm);
//        Permission previous = previousPermissions.remove(name);
//        if (previous != null) pluginManager.addPermission(previous);
//        for (String child : permission.getChildren().keySet())
//            unregisterPermission(pluginManager.getPermission(child));
//    }
//
//    /**
//     * Bukkit permission implementation associated with the current registry.
//     */
//    static final class BukkitPermission extends Permission {
//
//        /**
//         * Instantiates a new Bukkit permission.
//         *
//         * @param permissionInfo the permission info
//         * @param children       the children
//         */
//        public BukkitPermission(final @NotNull PermissionInfo permissionInfo,
//                                final @NotNull Collection<String> children) {
//            super(
//                    permissionInfo.getPermission(),
//                    getPermissionDefault(permissionInfo.getGrant()),
//                    children.stream().collect(Collectors.toMap(k -> k, k -> true))
//            );
//        }
//
//        /**
//         * Converts a {@link Grant} to a bukkit {@link PermissionDefault}.
//         *
//         * @param grant the grant
//         * @return the permission default
//         */
//        static @NotNull PermissionDefault getPermissionDefault(final @NotNull Grant grant) {
//            switch (grant) {
//                case ALL:
//                    return PermissionDefault.TRUE;
//                case NONE:
//                    return PermissionDefault.FALSE;
//                default:
//                    return PermissionDefault.OP;
//            }
//        }
//
//    }
//
//}
