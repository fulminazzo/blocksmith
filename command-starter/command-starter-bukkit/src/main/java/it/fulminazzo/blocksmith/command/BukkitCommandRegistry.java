package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.annotation.Permission.Grant;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.PermissionInfo;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CommandRegistry} for Bukkit platforms.
 */
class BukkitCommandRegistry extends CommandRegistry {
    private final @NotNull Server server;

    private final @NotNull SimpleCommandMap commandMap;
    private final @NotNull Map<String, Command> knownCommands;
    private final @NotNull Map<String, Command> previousCommands = new ConcurrentHashMap<>();

    private final @NotNull PluginManager pluginManager;
    private final @NotNull Map<String, Permission> previousPermissions = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Bukkit command registry.
     *
     * @param application the application that is initializing the registry
     */
    public BukkitCommandRegistry(final @NotNull ApplicationHandle application) {
        super(application);
        this.server = (Server) application.getServer();

        Reflect reflect = Reflect.on(server).fields().values().stream()
                .filter(f -> f.get() instanceof SimpleCommandMap)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find SimpleCommandMap"));
        this.commandMap = reflect.get();
        this.knownCommands = reflect.field("knownCommands").get();

        this.pluginManager = server.getPluginManager();
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(final @NotNull Object executor) {
        return new BukkitCommandSenderWrapper((CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        command.getAliases().forEach(a -> {
            Command curr = knownCommands.remove(a);
            if (curr != null) previousCommands.put(a, curr);
        });
        actualRegister(commandName, command);
        updateCommands();
    }

    /**
     * Handles actual registration of commands.
     *
     * @param commandName the command name
     * @param command     the command
     */
    protected void actualRegister(final @NonNull String commandName, final @NonNull LiteralNode command) {
        BukkitCommand cmd = new BukkitCommand(commandName, command);
        cmd.setPermission(registerPermission(command).getName());
        commandMap.register(commandName, getPrefix(), cmd);
    }

    /**
     * Registers a new bukkit Permission for the given node.
     *
     * @param node the node
     * @return the registered permission
     */
    @NotNull Permission registerPermission(final @NotNull LiteralNode node) {
        PermissionInfo permissionInfo = node.getCommandInfo().orElseThrow().getPermission();
        Set<String> childrenPermissions = getChildrenPermissions(node);
        Permission permission = new BukkitPermission(permissionInfo, childrenPermissions);
        Permission previous = pluginManager.getPermission(permission.getName());
        if (!(previous instanceof BukkitPermission)) {
            if (previous != null) {
                pluginManager.removePermission(previous);
                previousPermissions.put(permission.getName(), previous);
            }
            pluginManager.addPermission(permission);
        }
        return permission;
    }

    /**
     * Gets the children permissions of the given node.
     *
     * @param node the node
     * @return the permissions
     */
    @NotNull Set<String> getChildrenPermissions(final @NotNull CommandNode node) {
        Set<String> permissions = new HashSet<>();
        for (CommandNode child : node.getChildren()) {
            if (child instanceof LiteralNode) {
                Permission permission = registerPermission((LiteralNode) child);
                permissions.add(permission.getName());
            }
            permissions.addAll(getChildrenPermissions(child));
        }
        return permissions;
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        Command command = knownCommands.remove(getPrefix() + ":" + commandName);
        if (command != null) {
            unregisterPermission(command);
            removeOrRestoreCommand(command.getName());
            command.getAliases().forEach(this::removeOrRestoreCommand);
        }
        updateCommands();
    }

    /**
     * Unregisters the given permission.
     *
     * @param command the command
     */
    void unregisterPermission(final @NotNull Command command) {
        String permission = command.getPermission();
        if (permission != null) unregisterPermission(pluginManager.getPermission(permission));
    }

    /**
     * Unregisters the given permission.
     *
     * @param permission the permission
     */
    void unregisterPermission(final @Nullable Permission permission) {
        if (permission == null) return;
        String name = permission.getName();
        Permission perm = pluginManager.getPermission(name);
        if (!(perm instanceof BukkitPermission)) return;
        pluginManager.removePermission(perm);
        Permission previous = previousPermissions.remove(name);
        if (previous != null) pluginManager.addPermission(previous);
        for (String child : permission.getChildren().keySet())
            unregisterPermission(pluginManager.getPermission(child));
    }

    private void removeOrRestoreCommand(final @NotNull String alias) {
        Command cmd = previousCommands.remove(alias);
        Command current = knownCommands.remove(alias);
        if (cmd != null) {
            unregisterPermission(current);
            if (current instanceof BukkitCommand) knownCommands.put(alias, cmd);
        }
        knownCommands.remove(getPrefix() + ":" + alias);
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    /**
     * Updates the commands for the online players.
     */
    protected void updateCommands() {
        try {
            Method syncCommands = server.getClass().getDeclaredMethod("syncCommands");
            syncCommands.setAccessible(true);
            syncCommands.invoke(server);
        } catch (Exception ignored) {
        }
    }

    /**
     * Bukkit command implementation associated with the current registry.
     */
    final class BukkitCommand extends Command {
        private final @NotNull LiteralNode command;

        /**
         * Instantiates a new Bukkit command.
         *
         * @param commandName the command name
         * @param command     the root of the command route
         */
        public BukkitCommand(final @NotNull String commandName, final @NotNull LiteralNode command) {
            super(commandName,
                    command.getCommandInfo().orElseThrow().getDescription(),
                    "", //TODO: usage
                    command.getAliases().stream()
                            .filter(a -> !a.equals(commandName))
                            .collect(Collectors.toList())
            );
            this.command = command;
        }

        @Override
        public boolean execute(final @NonNull CommandSender sender,
                               final @NonNull String commandLabel,
                               final @NonNull String[] args) {
            BukkitCommandRegistry.this.execute(command, sender, commandLabel, args);
            return true;
        }

        @Override
        public @NonNull List<String> tabComplete(final @NonNull CommandSender sender,
                                                 final @NonNull String alias,
                                                 final @NonNull String[] args) throws IllegalArgumentException {
            return BukkitCommandRegistry.this.tabComplete(command, sender, alias, args);
        }

    }

    /**
     * The type Bukkit permission.
     */
    static final class BukkitPermission extends Permission {

        /**
         * Instantiates a new Bukkit permission.
         *
         * @param permissionInfo the permission info
         * @param children       the children
         */
        public BukkitPermission(final @NotNull PermissionInfo permissionInfo,
                                final @NotNull Collection<String> children) {
            super(
                    permissionInfo.getPermission(),
                    getPermissionDefault(permissionInfo.getGrant()),
                    children.stream().collect(Collectors.toMap(k -> k, k -> true))
            );
        }

        /**
         * Converts a {@link Grant} to a bukkit {@link PermissionDefault}.
         *
         * @param grant the grant
         * @return the permission default
         */
        static @NotNull PermissionDefault getPermissionDefault(final @NotNull Grant grant) {
            switch (grant) {
                case ALL:
                    return PermissionDefault.TRUE;
                case NONE:
                    return PermissionDefault.FALSE;
                default:
                    return PermissionDefault.OP;
            }
        }

    }

}
