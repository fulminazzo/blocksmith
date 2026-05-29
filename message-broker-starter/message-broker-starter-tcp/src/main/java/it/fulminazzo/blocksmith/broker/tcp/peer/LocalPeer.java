package it.fulminazzo.blocksmith.broker.tcp.peer;

import it.fulminazzo.blocksmith.broker.tcp.client.TcpMessageClient;
import it.fulminazzo.blocksmith.broker.tcp.server.TcpMessageServer;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A local peer node.
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
public final class LocalPeer implements Runnable, Closeable, PeerConnection {
    private final @NotNull ExecutorService serverExecutor = Executors.newSingleThreadExecutor(
            ThreadUtils.ownedThreadFactory(LocalPeer.class, true, "")
    );
    private final @NotNull ScheduledExecutorService clientExecutor = Executors.newSingleThreadScheduledExecutor(
            ThreadUtils.ownedThreadFactory(LocalPeer.class, true, "")
    );

    private final @NotNull List<Consumer<@NotNull String>> callbacks = new ArrayList<>();

    private final @NotNull Logger logger;
    private final @NotNull Mapper mapper;

    private final int port;
    private final @NotNull String channelName;

    private final long retryConnectionInterval;

    private @Nullable TcpMessageServer server;
    private @Nullable TcpMessageClient client;

    @Getter
    private boolean shutdown;

    /**
     * Instantiates a new Local peer.
     *
     * @param logger                  the logger to display messages
     * @param mapper                  the mapper to deserialize the messages
     * @param port                    the port to connect on
     * @param channelName             the channel name
     * @param retryConnectionInterval the time to wait before attempting to instantiate a new connection to the server
     */
    public LocalPeer(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final int port,
            final @NotNull String channelName,
            final long retryConnectionInterval
    ) {
        this.logger = logger;
        this.mapper = mapper;
        this.port = port;
        this.channelName = channelName;
        this.retryConnectionInterval = retryConnectionInterval;
    }

    /**
     * Starts this peer.
     */
    public void start() {
        clientExecutor.scheduleAtFixedRate(
                this,
                0,
                retryConnectionInterval,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void write(final @NotNull String message) {
        if (client != null) client.write(message);
    }

    @Override
    public @NotNull PeerConnection onRead(final @NotNull Consumer<@NotNull String> callback) {
        callbacks.add(callback);
        return this;
    }

    @Override
    public void run() {
        if (!isShutdown()) {
            logger.debug("Starting server on port {}", port);
            TcpMessageServer server = new TcpMessageServer(logger, mapper, port);
            serverExecutor.submit(server);
            clientExecutor.submit(() -> {
                if (!server.isClosed()) {
                    logger.debug("Server started on port {}", port);
                    this.server = server;
                } else logger.debug(
                        "Could not start server on port {} (probably due to other server already running)",
                        port
                );
            });
            try {
                logger.debug("Connecting to server on port {}", port);
                client = new TcpMessageClient(logger, mapper, port, channelName)
                        .onRead(s -> callbacks.forEach(c -> c.accept(s)));
                client.start();
            } catch (IOException e) {
                // do nothing, re-try connection on next iteration
            }
        }
    }

    @Override
    public void close() {
        if (server != null) server.close();
        if (client != null) client.close();
        shutdown = true;
        serverExecutor.shutdownNow();
        clientExecutor.shutdownNow();
    }

}
