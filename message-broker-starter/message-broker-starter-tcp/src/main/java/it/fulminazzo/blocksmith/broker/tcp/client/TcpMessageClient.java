package it.fulminazzo.blocksmith.broker.tcp.client;

import it.fulminazzo.blocksmith.broker.tcp.server.ChannelDto;
import it.fulminazzo.blocksmith.broker.tcp.server.TcpMessageServer;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.Getter;
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
public final class TcpMessageClient extends AbstractTcpMessageClient {
    @Getter
    private final @NotNull String channelName;
    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new TCP Message client.
     *
     * @param logger      the logger to display messages
     * @param mapper      the mapper to deserialize the messages
     * @param port        the port to connect on
     * @param channelName the channel name
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    public TcpMessageClient(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final int port,
            final @NotNull String channelName
    ) throws IOException {
        super(logger, new Socket("localhost", port));
        this.mapper = mapper;
        this.channelName = channelName;
    }

    /**
     * Sends the channel name and awaits a server response.
     * If the response is valid, the client will start reading messages.
     */
    public void start() {
        write(mapper.serialize(new ChannelDto(channelName)));
        String response = read();
        logger.debug(formatLog("Received server response to connection request: {}"), response);
        if (response != null && response.equals("OK")) {
            logger.info(formatLog("Connected to channel '{}'"), channelName);
            run();
        } else logger.warn(formatLog("Server did not respond to connection request"));
        close();
    }

}
