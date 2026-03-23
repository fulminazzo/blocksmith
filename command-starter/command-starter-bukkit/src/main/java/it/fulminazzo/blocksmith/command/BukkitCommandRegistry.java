package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class BukkitCommandRegistry extends CommandRegistry {
    private final @NotNull SimpleCommandMap commandMap;
    private final @NotNull Map<String, Command> knownCommands;
    private final @NotNull Map<String, Command> previousCommands = new ConcurrentHashMap<>();

    public BukkitCommandRegistry(final @NotNull BlocksmithApplication application) {
        super(application.getMessenger(), application.getLog(), application.getName().toLowerCase());
        Reflect reflect = Reflect.on(application.getServer()).fields().values().stream()
                .filter(f -> f.get() instanceof SimpleCommandMap)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find SimpleCommandMap"));
        this.commandMap = reflect.get();
        this.knownCommands = reflect.field("knownCommands").get();
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
        commandMap.register(
                commandName,
                getPrefix(),
                new BukkitCommand(commandName, command)
        );
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        Command command = knownCommands.remove(getPrefix() + ":" + commandName);
        if (command != null) {
            removeOrRestoreCommand(command.getName());
            command.getAliases().forEach(this::removeOrRestoreCommand);
        }
    }

    private void removeOrRestoreCommand(final @NotNull String alias) {
        Command cmd = previousCommands.get(alias);
        Command current = knownCommands.remove(alias);
        if (cmd != null && current instanceof BukkitCommand)
            knownCommands.put(alias, cmd);
        knownCommands.remove(getPrefix() + ":" + alias);
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

    private final class BukkitCommand extends Command {
        private final @NotNull LiteralNode command;

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

}
