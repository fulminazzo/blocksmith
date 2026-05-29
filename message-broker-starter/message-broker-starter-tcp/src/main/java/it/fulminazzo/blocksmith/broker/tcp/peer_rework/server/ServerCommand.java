package it.fulminazzo.blocksmith.broker.tcp.peer_rework.server;

import it.fulminazzo.blocksmith.broker.tcp.peer_rework.MessageDto;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Represents a command that can be executed by the server.
 *
 * @see TcpMessageServer
 * @see TcpMessageServerClient
 */
@RequiredArgsConstructor
public enum ServerCommand {
    /**
     * Subscribes to a channel.
     * <br>
     * Syntax: {@code SUBSCRIBE <channel>}
     */
    SUBSCRIBE((client, args) -> {
        if (!args.isEmpty()) client.subscribe(args.get(0)).send(ServerResponse.SUCCESS);
        else client.send(ServerResponse.NOT_ENOUGH_ARGUMENTS);
    }) {
        @Override
        public @NotNull String formatCommand(final @NotNull Object... arguments) {
            return String.format("%s %s", name(), arguments[0]);
        }
    },
    /**
     * Unsubscribes from a channel.
     * <br>
     * Syntax: {@code UNSUBSCRIBE <channel>}
     */
    UNSUBSCRIBE((client, args) -> {
        if (!args.isEmpty()) client.unsubscribe(args.get(0)).send(ServerResponse.SUCCESS);
        else client.send(ServerResponse.NOT_ENOUGH_ARGUMENTS);
    }) {
        @Override
        public @NotNull String formatCommand(final @NotNull Object... arguments) {
            return String.format("%s %s", name(), arguments[0]);
        }
    },
    /**
     * Sends a message to all the clients listening on the channel.
     * <br>
     * Syntax: {@code MESSAGE <channel> <message...>}
     */
    MESSAGE((client, args) -> {
        if (args.size() > 1) {
            String channel = args.get(0);
            if (client.isSubscribed(channel)) {
                String message = String.join(" ", args.subList(1, args.size()));
                Mapper mapper = client.getMapper();
                client.send(mapper.serialize(new MessageDto(channel, message)));
            }
        } else client.send(ServerResponse.NOT_ENOUGH_ARGUMENTS);
    }) {
        @Override
        public @NotNull String formatCommand(final Object @NotNull ... arguments) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < arguments.length; i++) {
                Object arg = arguments[i];
                builder.append(arg == null ? "null" : arg.toString()).append(" ");
            }
            return String.format("%s %s %s", name(), arguments[0], builder.toString().trim());
        }
    };

    private final @NotNull BiConsumer<TcpMessageServerClient, List<String>> executor;

    /**
     * Formats the command with the given arguments.
     *
     * @param arguments the arguments
     * @return the formatted command
     */
    public abstract @NotNull String formatCommand(final @NotNull Object... arguments);

    /**
     * Executes the command.
     *
     * @param client    the client
     * @param arguments the arguments for the command
     */
    void execute(final @NotNull TcpMessageServerClient client, final @NotNull String arguments) {
        executor.accept(
                client,
                StringUtils.split(arguments, " ", "'", "\"")
        );
    }

}
