package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CommandRegistry} for Bukkit platforms.
 */
class BukkitCommandRegistry extends CommandRegistry {
    private static final @NotNull String COMMAND_PREFIX = "/";

    protected final @NotNull Server server;

    private final @NotNull SimpleCommandMap commandMap;
    private final @NotNull Map<String, Command> knownCommands;
    private final @NotNull Map<String, Command> previousCommands = new ConcurrentHashMap<>();

    private final @NotNull Map<String, HelpTopic> helpMap;
    private final @NotNull Map<String, HelpTopic> previousTopics = new ConcurrentHashMap<>();

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

        this.helpMap = Reflect.on(server.getHelpMap()).get(f -> f.getType().isAssignableFrom(Map.class)).get();

        this.permissionRegistry = new BukkitPermissionRegistry(application);
    }

    @Override
    protected @NotNull CommandSenderWrapper<CommandSender> wrapSender(final @NotNull Object executor) {
        return new BukkitCommandSenderWrapper(application, (CommandSender) executor);
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

        for (String name : command.getAliases()) {
            registerInHelpMap(name, cmd);
            registerInHelpMap(getBukkitPrefix() + name, cmd);
        }
    }

    @Override
    protected void onRegister(final @NotNull String commandName, final @NotNull LiteralNode command) {
        registerInCommandMap(commandName, command);
        updateClientCommands();
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

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    /**
     * Alias for {@link #getPrefix()} + ":".
     *
     * @return the prefix
     */
    @NotNull String getBukkitPrefix() {
        return getPrefix() + ":";
    }

    private void registerInHelpMap(final @NotNull String name, final @NotNull BukkitCommand command) {
        String prefixedName = COMMAND_PREFIX + name;
        HelpTopic prev = helpMap.remove(prefixedName);
        if (prev != null) previousTopics.put(prefixedName, prev);
        helpMap.put(prefixedName, new GenericCommandHelpTopic(command));
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
            unregisterFromHelpMap(alias);
        }
        knownCommands.remove(getBukkitPrefix() + alias);
        unregisterFromHelpMap(getBukkitPrefix() + alias);
    }

    private void unregisterFromHelpMap(final @NotNull String name) {
        String prefixedName = COMMAND_PREFIX + name;
        HelpTopic prev = previousTopics.remove(prefixedName);
        if (prev != null) helpMap.put(prefixedName, prev);
        else helpMap.remove(prefixedName);
    }

    private void unregisterPermission(final @NotNull Command command) {
        permissionRegistry.unregisterPermission(command.getPermission());
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
                    LegacyComponentSerializer.legacySection().serialize(
                            ComponentUtils.toComponent(command.getUsage())
                    ),
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
