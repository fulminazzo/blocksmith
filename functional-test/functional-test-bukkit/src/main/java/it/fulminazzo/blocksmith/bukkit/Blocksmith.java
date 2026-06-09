package it.fulminazzo.blocksmith.bukkit;

import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.ExecutorWrapper;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
public final class Blocksmith extends JavaPlugin {
    private final @NotNull Logger logger = Reflect.on(JDK14LoggerAdapter.class)
            .init(getLogger())
            .get();
    private final @NotNull BlocksmithMain main = new BlocksmithMain(logger);

    @Override
    public void onEnable() {
        String commandName = getName().toLowerCase(Locale.ROOT);
        Objects.requireNonNull(
                getCommand(commandName),
                String.format("Could not get %s command.", commandName)
        ).setExecutor(this);

        main.enable();
    }

    @Override
    public void onDisable() {
        main.disable();
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        if (args.length == 0) sender.sendMessage("No subcommand specified");
        else main.executeCommand(
                new BukkitExecutorWrapper(sender),
                args[0],
                Arrays.copyOfRange(args, 1, args.length)
        );
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        if (args.length == 1)
            return main.getCommands().stream()
                    .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        else return Collections.emptyList();
    }

    private static final class BukkitExecutorWrapper implements ExecutorWrapper {
        private final @NotNull CommandSender sender;

        public BukkitExecutorWrapper(@NotNull CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void sendMessage(final @NotNull String message) {
            sender.sendMessage(message);
        }

        @Override
        public @NotNull String getName() {
            return sender.getName();
        }

    }

}
