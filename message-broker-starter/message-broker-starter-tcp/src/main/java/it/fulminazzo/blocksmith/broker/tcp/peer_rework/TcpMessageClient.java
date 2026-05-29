package it.fulminazzo.blocksmith.broker.tcp.peer_rework;

import it.fulminazzo.blocksmith.broker.tcp.peer_rework.server.TcpMessageServer;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * TCP client to connect to {@link TcpMessageServer}.
 * The message protocol is very basic as the main concern for this module is testing purposes.
 * In production environments a more sophisticated module should be used.
 *
 * @see TcpMessageServer
 */
public abstract class TcpMessageClient extends AbstractTcpMessageClient<TcpMessageClient> {

    /**
     * Instantiates a new TCP Message client.
     *
     * @param logger the logger used to display messages
     * @param mapper the mapper used to serialize the messages. Must be the same on the server
     * @param port   the port to connect on
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    public TcpMessageClient(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final int port
    ) throws IOException {
        super(logger, mapper, new Socket("localhost", port));
    }

    /**
     * Handles the message received from the server.
     *
     * @param channel the channel the message was published to
     * @param message the actual message
     */
    public abstract void handleMessage(final @NotNull String channel, final @NotNull String message);

    @Override
    protected void handleMessage(final @NotNull String message) {
        final MessageDto messageDto;
        try {
            messageDto = getMapper().deserialize(message, MessageDto.class);
        } catch (MapperException e) {
            logger.warn("Error while deserializing message: {}", e.getMessage());
            return;
        }
        handleMessage(messageDto.getChannel(), messageDto.getMessage());
    }

}
