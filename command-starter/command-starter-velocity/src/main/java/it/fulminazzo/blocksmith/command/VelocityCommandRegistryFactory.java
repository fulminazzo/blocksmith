package it.fulminazzo.blocksmith.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.command.visitor.usage.UsageStyle;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class VelocityCommandRegistryFactory implements CommandRegistryFactory {

    static {
        ArgumentParsers.register(Player.class, new ArgumentParser<>() {

            @Override
            public @NotNull Player parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                ProxyServer server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                return server.getPlayer(argument).orElseThrow(() -> new ArgumentParseException(CommandMessages.PLAYER_NOT_FOUND)
                        .arguments(Placeholder.of("player", argument)));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                ProxyServer server = visitor.getApplication().server();
                return server.getAllPlayers().stream()
                        .map(Player::getUsername)
                        .collect(Collectors.toList());
            }

        });
        UsageStyle.registerDefaultArgumentColor(Player.class, UsageStyle.DEFAULT_PLAYER_COLOR);
        ArgumentParsers.register(RegisteredServer.class, new ArgumentParser<>() {

            @Override
            public @NotNull RegisteredServer parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
                ProxyServer server = visitor.getApplication().server();
                String argument = visitor.getInput().getCurrent();
                return server.getServer(argument).orElseThrow(() -> new ArgumentParseException(CommandMessages.SERVER_NOT_FOUND)
                        .arguments(Placeholder.of("server", argument)));
            }

            @Override
            public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
                ProxyServer server = visitor.getApplication().server();
                return server.getAllServers().stream()
                        .map(RegisteredServer::getServerInfo)
                        .map(ServerInfo::getName)
                        .collect(Collectors.toList());
            }

        });
        UsageStyle.registerDefaultArgumentColor(RegisteredServer.class, UsageStyle.DEFAULT_SERVER_COLOR);
    }

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull ApplicationHandle application) {
        return new VelocityCommandRegistry(application);
    }

}
