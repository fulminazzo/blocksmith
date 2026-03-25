package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class BungeeCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(ProxiedPlayer.class, new ArgumentParser<>() {

            @Override
            public @NotNull ProxiedPlayer parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                String argument = context.getCurrent();
                ProxiedPlayer player = server.getPlayer(argument);
                if (player != null) return player;
                else throw new CommandExecutionException("error.player-not-found")
                        .arguments(Placeholder.of("player", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                return server.getPlayers().stream()
                        .map(ProxiedPlayer::getName)
                        .collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(ServerInfo.class, new ArgumentParser<>() {

            @Override
            public @NotNull ServerInfo parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                String argument = context.getCurrent();
                ServerInfo serverInfo = server.getServerInfo(argument);
                if (serverInfo != null) return serverInfo;
                else throw new CommandExecutionException("error.server-not-found")
                        .arguments(Placeholder.of("server", argument));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                return new ArrayList<>(server.getServers().keySet());
            }

        });
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return new BungeeCommandRegistry(application);
    }

}
