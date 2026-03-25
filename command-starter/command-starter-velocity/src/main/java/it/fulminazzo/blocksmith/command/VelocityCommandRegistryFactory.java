package it.fulminazzo.blocksmith.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class VelocityCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(Player.class, new ArgumentParser<>() {

            @Override
            public @NotNull Player parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                String argument = context.getCurrent();
                return server.getPlayer(argument).orElseThrow(() -> new CommandExecutionException("error.player-not-found")
                        .arguments(Placeholder.of("player", argument)));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                return server.getAllPlayers().stream()
                        .map(Player::getUsername)
                        .collect(Collectors.toList());
            }

        });
        ArgumentParsers.register(RegisteredServer.class, new ArgumentParser<>() {

            @Override
            public @NotNull RegisteredServer parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                String argument = context.getCurrent();
                return server.getServer(argument).orElseThrow(() -> new CommandExecutionException("error.server-not-found")
                        .arguments(Placeholder.of("server", argument)));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
                ProxyServer server = (ProxyServer) context.getApplication().getServer();
                return server.getAllServers().stream()
                        .map(RegisteredServer::getServerInfo)
                        .map(ServerInfo::getName)
                        .collect(Collectors.toList());
            }

        });
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return new VelocityCommandRegistry(application);
    }

}
