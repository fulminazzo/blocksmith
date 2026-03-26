package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Special implementation of {@link CommandRegistry} for Bukkit platforms with extended support for Brigadier.
 *
 * @param <S> the type parameter
 */
final class BrigadierBukkitCommandRegistry<S> extends BrigadierCommandRegistry<S> {
    private final @NotNull Server server;

    private final @NotNull RootCommandNode<S> root;
    private final @NotNull Map<String, CommandNode<S>> previousNodes = new ConcurrentHashMap<>();

    private final @NotNull BukkitPermissionRegistry permissionRegistry;
    private final @NotNull Map<String, Permission> registeredPermissions = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Bukkit command registry.
     *
     * @param application       the application that is initializing the registry
     * @param commandDispatcher the command dispatcher to register the commands with
     */
    @SuppressWarnings("unchecked")
    public BrigadierBukkitCommandRegistry(final @NotNull ApplicationHandle application,
                                          final @NotNull Object commandDispatcher) {
        super(application);
        this.server = (Server) application.getServer();

        this.root = ((CommandDispatcher<S>) commandDispatcher).getRoot();

        this.permissionRegistry = new BukkitPermissionRegistry(application);
    }

    @Override
    protected @NotNull CommandSenderWrapper wrapSender(@NotNull Object executor) {
        if (!(executor instanceof CommandSender))
            executor = Reflect.on(executor).call("getBukkitSender").get();
        return new BukkitCommandSenderWrapper((CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName,
                              final @NotNull LiteralNode command,
                              final @NotNull LiteralCommandNode<S> brigadierCommand) {
        CommandNode<S> previous = root.getChild(commandName);
        if (previous != null) {
            previousNodes.put(commandName, previous);
            getChildren().remove(commandName);
            getLiterals().remove(commandName);
        }
        root.addChild(brigadierCommand);
        if (!commandName.startsWith(getBukkitPrefix())) {
            registeredPermissions.put(commandName, permissionRegistry.registerPermission(command));
            String prefixedCommandName = getBukkitPrefix() + commandName;
            onRegister(prefixedCommandName, command.clone(prefixedCommandName));
        }
        updateCommands();
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        if (!commandName.startsWith(getBukkitPrefix())) {
            Permission permission = registeredPermissions.remove(commandName);
            if (permission != null) permissionRegistry.unregisterPermission(permission);
            onUnregister(getBukkitPrefix() + commandName);
        }
        getChildren().remove(commandName);
        getLiterals().remove(commandName);
        CommandNode<S> previous = previousNodes.remove(commandName);
        if (previous != null) root.addChild(previous);
        updateCommands();
    }

    private @NonNull String getBukkitPrefix() {
        return getPrefix() + ":";
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    private @NotNull Map<String, CommandNode<S>> getChildren() {
        return Reflect.on(root).field("children").get();
    }

    private @NotNull Map<String, CommandNode<S>> getLiterals() {
        return Reflect.on(root).field("literals").get();
    }

    private void updateCommands() {
        try {
            Method syncCommands = server.getClass().getDeclaredMethod("syncCommands");
            syncCommands.setAccessible(true);
            syncCommands.invoke(server);
        } catch (Exception ignored) {
        }
    }

}
