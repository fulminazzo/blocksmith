package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.argument.CompositeArgumentParser;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class BungeeCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(ProxiedPlayer.class, new ArgumentParser<>() {

            @Override
            public @NotNull ProxiedPlayer parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                ProxyServer server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                ProxiedPlayer player = server.getPlayer(argument);
                if (player != null) return player;
                else throw new ArgumentParseException(CommandMessages.PLAYER_NOT_FOUND)
                        .arguments(Placeholder.of("player", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                ProxyServer server = visitor.getApplication().server();
                return server.getPlayers().stream()
                        .map(ProxiedPlayer::getName)
                        .collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(CommandSender.class, new CompositeArgumentParser<>(
                new ArgumentParser<>() {

                    @Override
                    public @Nullable CommandSender parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                        String current = visitor.getInput().getCurrent();
                        if (current.equals(ArgumentParsers.CONSOLE_COMMAND_NAME)) {
                            ProxyServer server = visitor.getApplication().server();
                            return server.getConsole();
                        } else throw new ArgumentParseException(CommandMessages.UNRECOGNIZED_ARGOMENT)
                                .arguments(
                                        Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, current),
                                        Placeholder.of("expected", ArgumentParsers.CONSOLE_COMMAND_NAME)
                                );
                    }

                    @Override
                    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                        return List.of(ArgumentParsers.CONSOLE_COMMAND_NAME);
                    }

                },
                ArgumentParsers.of(ProxiedPlayer.class)
        ));
        ArgumentParsers.register(ServerInfo.class, new ArgumentParser<>() {

            @Override
            public @NotNull ServerInfo parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                ProxyServer server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                ServerInfo serverInfo = server.getServerInfo(argument);
                if (serverInfo != null) return serverInfo;
                else throw new ArgumentParseException(CommandMessages.SERVER_NOT_FOUND)
                        .arguments(Placeholder.of("server", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                ProxyServer server = visitor.getApplication().server();
                return new ArrayList<>(server.getServers().keySet());
            }

        });
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return new BungeeCommandRegistry(application);
    }

}
