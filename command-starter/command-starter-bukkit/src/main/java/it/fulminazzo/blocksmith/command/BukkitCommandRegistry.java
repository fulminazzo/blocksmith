package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CommandRegistry} for Bukkit platforms.
 */
class BukkitCommandRegistry extends CommandRegistry {
    protected final @NotNull Server server;

    private final @NotNull SimpleCommandMap commandMap;
    private final @NotNull Map<String, Command> knownCommands;
    private final @NotNull Map<String, Command> previousCommands = new ConcurrentHashMap<>();

    private final @NotNull BukkitPermissionRegistry permissionRegistry;

    /**
     * Instantiates a new Bukkit command registry.
     *
     * @param application the application that is initializing the registry
     */
    public BukkitCommandRegistry(final @NotNull ApplicationHandle application) {
        super(application);
        this.server = application.server();

        Reflect reflect = Reflect.on(server).get(f -> SimpleCommandMap.class.isAssignableFrom(f.getType()));
        this.commandMap = reflect.get();
        this.knownCommands = reflect.get("knownCommands").get();

        this.permissionRegistry = new BukkitPermissionRegistry(application);
    }

    @Override
    public @NotNull CommandSenderWrapper<CommandSender> wrapSender(final @NotNull Object executor) {
        return new BukkitCommandSenderWrapper(application, (CommandSender) executor);
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        registerInCommandMap(commandName, command);
        updateClientCommands();
    }

    /**
     * Registers the command in the Bukkit command map.
     *
     * @param commandName the command name
     * @param command     the command
     */
    protected void registerInCommandMap(final @NotNull String commandName, final @NotNull LiteralNode command) {
        command.getAliases().forEach(a -> {
            Command curr = knownCommands.remove(a);
            if (curr != null) previousCommands.put(a, curr);
        });
        BukkitCommand cmd = new BukkitCommand(commandName, command);
        cmd.setPermission(permissionRegistry.registerPermission(command).getName());
        commandMap.register(commandName, getPrefix(), cmd);
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        Command command = knownCommands.remove(getBukkitPrefix() + commandName);
        if (command != null) {
            unregisterPermission(command);
            removeOrRestoreCommand(command.getName());
            command.getAliases().forEach(this::removeOrRestoreCommand);
        }
        updateClientCommands();
    }

    /**
     * Alias for {@link #getPrefix()} + ":".
     *
     * @return the prefix
     */
    @NotNull String getBukkitPrefix() {
        return getPrefix() + ":";
    }

    private void unregisterPermission(final @NotNull Command command) {
        permissionRegistry.unregisterPermission(command.getPermission());
    }

    private void removeOrRestoreCommand(final @NotNull String alias) {
        Command cmd = previousCommands.remove(alias);
        Command current = knownCommands.get(alias);
        if (current instanceof BukkitCommand) {
            knownCommands.remove(alias);
            if (cmd != null) {
                unregisterPermission(current);
                knownCommands.put(alias, cmd);
            }
        }
        knownCommands.remove(getBukkitPrefix() + alias);
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    private void updateClientCommands() {
        try {
            Reflect.on(server).invoke("syncCommands");
        } catch (ReflectException ignored) {
            // legacy version where syncCommands() is not available
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
                    command.getCommandInfo().getDescription(),
                    "", //TODO: usage
                    command.getAliases().stream()
                            .filter(a -> !a.equals(commandName))
                            .collect(Collectors.toList())
            );
            this.command = command;
        }

        @Override
        public boolean execute(final @NotNull CommandSender sender,
                               final @NotNull String commandLabel,
                               final @NotNull String[] args) {
            BukkitCommandRegistry.this.execute(command, sender, commandLabel, args);
            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(final @NotNull CommandSender sender,
                                                 final @NotNull String alias,
                                                 final @NotNull String[] args) throws IllegalArgumentException {
            return BukkitCommandRegistry.this.tabComplete(command, sender, alias, args);
        }

    }

}
