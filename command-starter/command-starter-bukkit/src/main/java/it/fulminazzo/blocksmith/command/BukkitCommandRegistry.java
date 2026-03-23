package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import it.fulminazzo.blocksmith.command.node.CommandInfo;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class BukkitCommandRegistry extends CommandRegistry {
    private final @NotNull SimpleCommandMap commandMap;
    private final @NotNull Map<String, Command> knownCommands;

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
        CommandInfo info = command.getCommandInfo().orElseThrow();
        List<String> aliases = new ArrayList<>(command.getAliases());
        aliases.remove(commandName);
        knownCommands.remove(commandName);
        commandMap.register(
                commandName,
                getPrefix(),
                new Command(
                        commandName,
                        info.getDescription(),
                        "", //TODO: usage
                        aliases
                ) {

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
        );
    }

    @Override
    protected void onUnregister(final @NotNull String commandName) {
        Command command = knownCommands.remove(commandName);
        if (command != null)
            command.getAliases().forEach(knownCommands::remove);
        command = knownCommands.remove(getPrefix() + ":" + commandName);
        if (command != null) {
            command.getAliases().forEach(a -> {
                knownCommands.remove(a);
                knownCommands.remove(getPrefix() + ":" + a);
            });
        }
    }

    @Override
    protected @NotNull Class<?> getSenderType() {
        return CommandSender.class;
    }

}
