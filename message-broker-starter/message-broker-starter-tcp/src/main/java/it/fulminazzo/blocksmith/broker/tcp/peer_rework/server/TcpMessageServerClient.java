package it.fulminazzo.blocksmith.broker.tcp.peer_rework.server;

import it.fulminazzo.blocksmith.broker.tcp.peer_rework.AbstractTcpMessageClient;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

/**
 * Handles a single TCP client connection to the server.
 *
 * @see AbstractTcpMessageClient
 * @see TcpMessageServer
 */
final class TcpMessageServerClient extends AbstractTcpMessageClient<TcpMessageServerClient> {

    /**
     * Instantiates a new TCP Message server client.
     *
     * @param logger the logger used to display messages
     * @param mapper the mapper used to serialize the messages. Must be the same on the client
     * @param socket the socket connection
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    public TcpMessageServerClient(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final @NotNull Socket socket
    ) throws IOException {
        super(logger, mapper, socket);
    }

    @Override
    protected void handleMessage(final @NotNull String message) {
        int index = message.indexOf(' ');
        if (index != -1) {
            String command = message.substring(0, index);
            String payload = message.substring(index + 1);
            ServerCommand serverCommand = null;
            try {
                serverCommand = ServerCommand.valueOf(command.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                send(ServerResponse.UNKNOWN_COMMAND);
            }
            if (serverCommand != null) serverCommand.execute(this, payload);
        } else send(ServerResponse.INVALID_REQUEST);
    }

}
