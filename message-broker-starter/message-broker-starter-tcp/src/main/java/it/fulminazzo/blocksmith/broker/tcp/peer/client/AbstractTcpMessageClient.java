package it.fulminazzo.blocksmith.broker.tcp.peer.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.fulminazzo.blocksmith.broker.tcp.peer.ChannelSubscriber;
import it.fulminazzo.blocksmith.broker.tcp.peer.Loggable;
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpConnection;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstraction of a TCP message client with common logic.
 *
 * @param <C> the type of the client (for method chaining)
 * @see TcpConnection
 * @see Loggable
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTcpMessageClient<C extends AbstractTcpMessageClient<C>>
        extends Loggable
        implements TcpConnection, Runnable, ChannelSubscriber<C> {
    private final @NotNull Set<String> channels = new HashSet<>();

    @Getter
    private final @NotNull Mapper mapper;

    private final @NotNull Socket socket;
    private final @NotNull BufferedReader input;
    private final @NotNull BufferedWriter output;

    @Getter
    private volatile boolean closed;

    /**
     * Instantiates a new Abstract TCP Message client.
     *
     * @param logger the logger used to display messages
     * @param mapper the mapper used to serialize the messages
     * @param socket the socket connection
     * @throws IOException in case it is not possible to retrieve the data streams
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "The socket comes directly from the SocketServer#accept() return method "
                    + "in the case of server clients"
    )
    public AbstractTcpMessageClient(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final @NotNull Socket socket
    ) throws IOException {
        super(logger);
        this.mapper = mapper;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Handles the message received from the peer.
     *
     * @param message the message received
     */
    protected abstract void handleMessage(final @NotNull String message);

    @Override
    public void send(final @NotNull String message) {
        try {
            output.write(message);
            output.newLine();
            output.flush();
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public void run() {
        logger.info(formatLog("New connection established"));
        String line;
        try {
            while ((line = input.readLine()) != null) {
                logger.debug(formatLog("Received message: {}"), line);
                handleMessage(line);
            }
        } catch (IOException e) {
            // connection dropped or client closed, ignore the error
        }
        close();
    }

    @Override
    public synchronized void close() {
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

    @Override
    public @NotNull C subscribe(final @NotNull String channel) {
        channels.add(channel);
        logger.info(formatLog("Subscribed to channel: {}"), channel);
        return (C) this;
    }

    @Override
    public @NotNull C unsubscribe(final @NotNull String channel) {
        channels.remove(channel);
        logger.info(formatLog("Unsubscribed from channel: {}"), channel);
        return (C) this;
    }

    @Override
    public boolean isSubscribed(final @NotNull String channel) {
        return channels.contains(channel);
    }

    @Override
    public @NotNull String getHost() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public int getPort() {
        return socket.getPort();
    }

    @Override
    protected @NotNull String formatLog(final @NotNull String message) {
        return String.format(
                "|%s (%s:%s)| %s",
                getClass().getSimpleName(),
                getHost(),
                getPort(),
                message
        );
    }

    @SuppressWarnings("checkstyle:NoFinalizer")
    @Override
    protected final void finalize() {
        // Requested from SpotBugs to avoid finalizer attacks.
    }

}
