package it.fulminazzo.blocksmith.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.ExecutorWrapper;
import it.fulminazzo.blocksmith.ProjectInfo;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
@SuppressWarnings("checkstyle:MissingJavadocMethod") // for events
public final class Blocksmith {
    private final @NotNull ProxyServer server;
    private final @NotNull BlocksmithMain main;

    /**
     * Instantiates this class.
     *
     * @param server the server
     * @param logger the logger
     */
    @Inject
    public Blocksmith(final @NotNull ProxyServer server, final @NotNull Logger logger) {
        this.server = server;
        this.main = new BlocksmithMain(logger);
    }

    @Subscribe
    public void onEnable(final @NotNull ProxyInitializeEvent event) {
        server.getCommandManager().register(
                ProjectInfo.PROJECT_NAME,
                new BlocksmithCommand(),
                "bs"
        );

        main.enable();
    }

    @Subscribe
    public void onDisable(final @NotNull ProxyInitializeEvent event) {
        main.disable();
    }

    private static final class VelocityExecutorWrapper implements ExecutorWrapper {
        private final CommandSource sender;

        public VelocityExecutorWrapper(CommandSource sender) {
            this.sender = sender;
        }

        @Override
        public void sendMessage(final @NotNull String message) {
            sender.sendMessage(Component.text(message));
        }

        @Override
        public @NotNull String getName() {
            return sender instanceof Player ? ((Player) sender).getUsername() : "CONSOLE";
        }

    }

    private class BlocksmithCommand implements SimpleCommand {

        @Override
        public void execute(final @NotNull Invocation invocation) {
            CommandSource sender = invocation.source();
            String @NonNull [] arguments = invocation.arguments();
            if (arguments.length == 0)
                sender.sendMessage(Component.text("No subcommand specified"));
            else main.executeCommand(
                    new VelocityExecutorWrapper(sender),
                    arguments[0],
                    Arrays.copyOfRange(arguments, 1, arguments.length)
            );
        }

        @Override
        public List<String> suggest(final @NotNull Invocation invocation) {
            String @NonNull [] arguments = invocation.arguments();
            if (arguments.length == 1)
                return main.getCommands().stream()
                        .filter(c -> c.toLowerCase(Locale.ROOT)
                                .startsWith(arguments[0].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            else return Collections.emptyList();
        }

    }

}
