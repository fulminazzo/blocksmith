package it.fulminazzo.blocksmith.bungee;

import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.ExecutorWrapper;
import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
public final class Blocksmith extends Plugin {
    private final @NotNull Logger logger = Reflect.on(JDK14LoggerAdapter.class)
            .init(getLogger())
            .get();
    private final @NotNull BlocksmithMain main = new BlocksmithMain(
            this,
            getDataFolder(),
            logger
    );

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new BlocksmithCommand());

        main.enable();
    }

    @Override
    public void onDisable() {
        main.disable();
    }

    private static final class BungeecordExecutorWrapper implements ExecutorWrapper {
        private final @NotNull CommandSender sender;

        public BungeecordExecutorWrapper(@NotNull CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void sendMessage(final @NotNull String message) {
            sender.sendMessage(TextComponent.fromLegacyText(message));
        }

        @Override
        public @NotNull String getName() {
            return sender.getName();
        }

    }

    private final class BlocksmithCommand extends Command implements TabExecutor {

        public BlocksmithCommand() {
            super(ProjectInfo.PROJECT_NAME, null, "bs");
        }

        @Override
        public void execute(final @NotNull CommandSender sender, final @NotNull String[] args) {
            if (args.length == 0)
                sender.sendMessage(TextComponent.fromLegacyText("No subcommand specified"));
            else main.executeCommand(
                    new BungeecordExecutorWrapper(sender),
                    args[0],
                    Arrays.copyOfRange(args, 1, args.length)
            );
        }

        @Override
        public @NotNull Iterable<String> onTabComplete(
                final @NotNull CommandSender sender,
                final @NotNull String[] args
        ) {
            if (args.length == 1)
                return main.getCommands().stream()
                        .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            else return Collections.emptyList();
        }

    }

}
