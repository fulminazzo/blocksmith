package it.fulminazzo.blocksmith.broker.tcp.server;

import it.fulminazzo.blocksmith.broker.tcp.AbstractTcpMessageClient;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handles a single client connection.
 *
 * @see TcpMessageServer
 */
final class TcpMessageClientHandler extends AbstractTcpMessageClient {
    private final @NotNull ExecutorService executor = Executors.newSingleThreadExecutor(
            ThreadUtils.ownedThreadFactory(TcpMessageClientHandler.class, true, "")
    );
    private final @NotNull Mapper mapper;

    private @Nullable String channelName;

    /**
     * Instantiates a new TCP Message client handler.
     *
     * @param logger the logger to display messages
     * @param mapper the mapper to deserialize the messages
     * @param socket the actual socket connection to the client
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    public TcpMessageClientHandler(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final @NotNull Socket socket
    ) throws IOException {
        super(logger, socket);
        this.mapper = mapper;
    }

    /**
     * Finalizes the connection and starts the message reading.
     * <br>
     * The connection requires the client to provide a {@link #channelName} before sending any messages.
     *
     * @param channelNameConsumer the consumer to be notified when the channel name is received
     */
    public void start(
            final @NotNull BiConsumer<@NotNull String, @NotNull TcpMessageClientHandler> channelNameConsumer
    ) {
        CompletableFuture.runAsync(() -> {
            String raw = read();
            logger.debug(formatLog("Received connection request: {}"), raw);
            if (raw != null)
                try {
                    ChannelDto channelDto = mapper.deserialize(raw, ChannelDto.class);
                    this.channelName = channelDto.getChannelName();
                    channelNameConsumer.accept(channelName, this);
                    logger.info(formatLog("Connected and listening on channel '{}'"), channelName);
                    if (channelName != null) run();
                    return;
                } catch (MapperException e) {
                    // handled immediately below
                }
            // invalid connection, notify client and quit
            logger.warn(formatLog("Invalid connection request"));
            write("Invalid connection. Please provide a channel name before sending any message.");
            close();
        }, executor);
    }

    /**
     * Sets the callback to be executed when a message is received.
     *
     * @param onRead the callback
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessageClientHandler onRead(
            final @NotNull BiConsumer<@NotNull String, @NotNull String> onRead
    ) {
        return onRead(m -> onRead.accept(getChannelName(), m));
    }

    private @NotNull String getChannelName() {
        return Objects.requireNonNull(
                channelName,
                "channel name has not been determined yet or was not provided"
        );
    }

    @Override
    public @NotNull TcpMessageClientHandler onRead(
            final @NotNull Consumer<@NotNull String> onRead
    ) {
        return (TcpMessageClientHandler) super.onRead(onRead);
    }

    @Override
    public void close() {
        executor.shutdownNow();
        super.close();
    }

    @Override
    protected @NotNull String formatLog(final @NotNull String message) {
        return String.format(
                "|TCP Client (%s:%s) [%s]|: %s",
                getHost(),
                getPort(),
                channelName,
                message
        );
    }

}
