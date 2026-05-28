package it.fulminazzo.blocksmith.broker.tcp.server;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperException;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Handles a single client connection.
 *
 * @see TcpMessageServer
 */
final class TcpMessageClientHandler implements Runnable, Closeable {
    private final @NotNull ExecutorService executor = Executors.newSingleThreadExecutor(
            ThreadUtils.ownedThreadFactory(TcpMessageClientHandler.class, true, "")
    );
    private final @NotNull Logger logger;
    private final @NotNull Mapper mapper;

    private final @NotNull Socket socket;
    private final @NotNull BufferedReader input;
    private final @NotNull BufferedWriter output;

    private @NotNull BiConsumer<@NotNull String, @NotNull String> onRead = (c, m) -> {
    };

    private @Nullable String channelName;
    @Getter
    private boolean closed;

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
        this.logger = logger;
        this.mapper = mapper;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
     * Reads a single line from the input stream.
     *
     * @return the line (or {@code null} if the connection is closed)
     */
    public @Nullable String read() {
        try {
            return input.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Attempts to send a message to the output stream.
     * It will <b>not throw</b> if the message could not be delivered.
     *
     * @param message the message
     */
    public void write(final @NotNull String message) {
        try {
            output.write(message);
            output.flush();
        } catch (IOException e) {
            // do nothing
        }
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
        this.onRead = onRead;
        return this;
    }

    private @NotNull String formatLog(final @NotNull String message) {
        return String.format(
                "|TCP Client (%s:%s)|: %s",
                getHost(),
                getPort(),
                message
        );
    }

    private @NotNull String getChannelName() {
        return Objects.requireNonNull(
                channelName,
                "channel name has not been determined yet or was not provided"
        );
    }

    private @NotNull String getHost() {
        return socket.getInetAddress().getHostAddress();
    }

    private int getPort() {
        return socket.getPort();
    }

    @Override
    public void run() {
        String line;
        while ((line = read()) != null) {
            logger.debug(formatLog("Received message: {}"), line);
            onRead.accept(getChannelName(), line);
        }
        closed = true;
    }

    @Override
    public void close() {
        executor.shutdownNow();
        try {
            output.close();
        } catch (IOException e) {
            // do nothing
        }
        try {
            input.close();
        } catch (IOException e) {
            // do nothing
        }
        try {
            socket.close();
        } catch (IOException e) {
            // do nothing
        }
        logger.info(formatLog("Connection closed"));
    }

}
