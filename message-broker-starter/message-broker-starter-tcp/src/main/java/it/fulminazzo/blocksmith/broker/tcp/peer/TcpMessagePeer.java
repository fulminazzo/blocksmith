package it.fulminazzo.blocksmith.broker.tcp.peer;

import it.fulminazzo.blocksmith.broker.tcp.peer.client.TcpMessageClient;
import it.fulminazzo.blocksmith.broker.tcp.peer.server.TcpMessageServer;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Local peer for handling TCP connections across the same machine.
 * <br>
 * The peer will handle simultaneously <b>server</b> and <b>client</b>.
 * <ul>
 *     <li>First, it will attempt to run a new server.
 *     If this fails, it is probably because another peer is already running the server on the machine;</li>
 *     <li>Then, it starts the actual client.
 *     The client does not know if the server was started by its peer or not, and it does not have to,
 *     as it adds no extra functionality: all functionality must run through the connection stream.</li>
 * </ul>
 * The peer will also handle <b>server shutdowns</b> by re-trying to run it.
 *
 * @see TcpMessageServer
 * @see TcpMessageClient
 */
public final class TcpMessagePeer extends Loggable implements ChannelSubscriber<TcpMessagePeer>, Runnable, Closeable {
    private static final @NotNull ThreadFactory THREAD_FACTORY = ThreadUtils.ownedThreadFactory(TcpMessagePeer.class);
    /**
     * How many milliseconds the client should wait for the server to either start or fail to start
     * (and therefore connect to another running instance).
     */
    private static final int AWAIT_SERVER_BOOT_TIME = 125;
    /**
     * How many milliseconds the {@link #start()} method should wait for the client to connect to the server.
     */
    private static final int AWAIT_CLIENT_BOOT_TIME = 125;

    private final @NotNull Map<String, Set<MessageHandler>> messageHandlers = new ConcurrentHashMap<>();
    private final @NotNull Set<String> channels = new CopyOnWriteArraySet<>();

    private final @NotNull Mapper mapper;
    private final int port;

    private final long retryInterval;

    private final @NotNull ExecutorService executor;

    private @Nullable TcpMessageServer server;
    private @Nullable TcpMessageClient client;

    @Getter
    private boolean closed = false;

    /**
     * Instantiates a new TCP Message peer.
     *
     * @param logger        the logger used to display messages
     * @param mapper        the mapper used to serialize the messages
     * @param port          the port to connect on
     * @param retryInterval the time to wait before trying to reconnect to the server (in milliseconds)
     * @param executor      the executor to handle internal tasks with
     */
    public TcpMessagePeer(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final int port,
            final long retryInterval,
            @NotNull ExecutorService executor
    ) {
        super(logger);
        this.mapper = mapper;
        this.port = port;
        this.retryInterval = retryInterval;
        this.executor = executor;
    }

    /**
     * Starts the peer.
     *
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessagePeer start() {
        Thread clientThread = THREAD_FACTORY.newThread(this);
        clientThread.start();
        try {
            clientThread.join(AWAIT_CLIENT_BOOT_TIME);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    /**
     * Registers a new handler for the specified channel.
     *
     * @param channel        the channel name
     * @param messageHandler the handler to register
     */
    public void registerHandler(final @NotNull String channel, final @NotNull MessageHandler messageHandler) {
        messageHandlers.computeIfAbsent(channel, c -> new CopyOnWriteArraySet<>()).add(messageHandler);
    }

    /**
     * Unregisters the handler from all the channels.
     *
     * @param messageHandler the handler to unregister
     */
    @SuppressWarnings("resource")
    public void unregisterHandler(final @NotNull MessageHandler messageHandler) {
        for (Map.Entry<String, Set<MessageHandler>> entry : messageHandlers.entrySet()) {
            Set<MessageHandler> handlers = entry.getValue();
            handlers.remove(messageHandler);
            if (handlers.isEmpty()) {
                String channelName = entry.getKey();
                messageHandlers.remove(channelName);
                unsubscribe(channelName);
            }
        }
    }

    /**
     * Gets the internal server, if present.
     *
     * @return the server
     */
    @NotNull Optional<TcpMessageServer> server() {
        return Optional.ofNullable(server);
    }

    /**
     * Gets the internal client, if present.
     *
     * @return the client
     */
    @NotNull Optional<TcpMessageClient> client() {
        return Optional.ofNullable(client);
    }

    private void closeConnections() {
        server().ifPresent(TcpMessageServer::close);
        server = null;

        client().ifPresent(TcpMessageClient::close);
        client = null;
    }

    @Override
    public void send(final @NotNull String message) {
        client().ifPresent(c -> c.send(message));
    }

    @Override
    public void run() {
        while (!isClosed()) {
            Thread serverThread = THREAD_FACTORY.newThread(() -> {
                try {
                    logger.debug(formatLog("Attempting to start TCP server"));
                    server = new TcpMessageServer(logger, mapper, port, executor);
                    server.run();
                } catch (IOException e) {
                    // server already running or port already in use, ignore the error
                    logger.debug(formatLog("TCP server already running or port already in use"));
                }
            });
            serverThread.start();
            try {
                serverThread.join(AWAIT_SERVER_BOOT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            try {
                logger.debug(formatLog("Starting client"));
                client = new TcpMessageClient(
                        logger,
                        mapper,
                        port
                ) {

                    @Override
                    public void handleMessage(final @NotNull String channel, final @NotNull String message) {
                        messageHandlers.getOrDefault(channel, Collections.emptySet())
                                .forEach(h -> h.handle(message));
                    }

                };
                channels.forEach(client::subscribe);
                client.run();
            } catch (IOException e) {
                logger.warn(formatLog("Failed to start client: {}"), e.getMessage(), e);
            }
            closeConnections();
            logger.info(formatLog("Attempting to restart TCP peer in {} seconds"), retryInterval / 1000);
        }
    }

    @Override
    public void close() {
        closed = true;
        closeConnections();
    }

    @Override
    public @NotNull TcpMessagePeer subscribe(final @NotNull String channel) {
        channels.add(channel);
        client().ifPresent(c -> c.subscribe(channel));
        return this;
    }

    @Override
    public @NotNull TcpMessagePeer unsubscribe(final @NotNull String channel) {
        channels.remove(channel);
        client().ifPresent(c -> c.unsubscribe(channel));
        return this;
    }

    @Override
    public boolean isSubscribed(final @NotNull String channel) {
        return channels.contains(channel);
    }

    @Override
    protected @NotNull String formatLog(@NotNull String message) {
        return String.format("|%s (%s)| %s", getClass().getSimpleName(), port, message);
    }

}
