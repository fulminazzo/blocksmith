package it.fulminazzo.blocksmith.broker.tcp.client;

import it.fulminazzo.blocksmith.broker.tcp.peer.PeerConnection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Abstraction of a TCP message client with common logic.
 */
public abstract class AbstractTcpMessageClient implements PeerConnection, Runnable, Closeable {
    protected final @NotNull Logger logger;

    private final @NotNull Socket socket;
    private final @NotNull BufferedReader input;
    private final @NotNull BufferedWriter output;

    private @NotNull Consumer<@NotNull String> onRead = (m) -> {
    };

    @Getter
    private boolean closed;

    /**
     * Instantiates a new TCP Message client.
     *
     * @param logger the logger to display messages
     * @param socket the actual socket connection to the client
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    public AbstractTcpMessageClient(
            final @NotNull Logger logger,
            final @NotNull Socket socket
    ) throws IOException {
        this.logger = logger;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Gets the channel name.
     *
     * @return the channel name
     */
    public abstract @NotNull String getChannelName();

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
     * Gets the host to which the client is connected.
     *
     * @return the host
     */
    public @NotNull String getHost() {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * Gets the port on which the client is connected.
     *
     * @return the port
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Formats the message to the log format.
     *
     * @param message the message
     * @return the formatted message
     */
    protected @NotNull String formatLog(final @NotNull String message) {
        return String.format(
                "|%s (%s:%s) [%s]|: %s",
                getClass().getSimpleName(),
                getHost(),
                getPort(),
                getChannelName(),
                message
        );
    }

    @Override
    public void write(final @NotNull String message) {
        try {
            output.write(message);
            output.newLine();
            output.flush();
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public @NotNull AbstractTcpMessageClient onRead(
            final @NotNull Consumer<@NotNull String> onRead
    ) {
        this.onRead = onRead;
        return this;
    }

    @Override
    public void run() {
        String line;
        while ((line = read()) != null) {
            logger.debug(formatLog("Received message: {}"), line);
            onRead.accept(line);
        }
        close();
    }

    @Override
    public void close() {
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
        if (!isClosed()) {
            logger.info(formatLog("Connection closed"));
            closed = true;
        }
    }

}
