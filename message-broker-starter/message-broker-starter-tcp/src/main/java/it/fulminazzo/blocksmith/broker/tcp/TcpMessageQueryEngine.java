package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import it.fulminazzo.blocksmith.broker.tcp.peer.MessageHandler;
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpMessagePeer;
import it.fulminazzo.blocksmith.broker.tcp.peer.server.ServerCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A message query engine with TCP support.
 * <br>
 * Uses {@link TcpMessagePeer} under the hood to send and receive messages.
 *
 * @see TcpMessagePeer
 * @see TcpMessageChannel
 * @see TcpMessageBroker
 */
public final class TcpMessageQueryEngine extends MessageQueryEngine implements MessageHandler {
    private final @NotNull List<Consumer<String>> handlers = new CopyOnWriteArrayList<>();

    private final @NotNull TcpMessagePeer connection;
    private final @NotNull ExecutorService executor;

    /**
     * Instantiates a new Tcp message query engine.
     *
     * @param channelName the channel name
     * @param connection  the connection used for sending and receiving
     * @param executor    the executor
     */
    TcpMessageQueryEngine(
            final @NotNull String channelName,
            final @NotNull TcpMessagePeer connection,
            final @NotNull ExecutorService executor
    ) {
        super(channelName);
        this.connection = connection.subscribe(channelName);
        this.executor = executor;
    }

    @Override
    public void handle(final @NotNull String message) {
        handlers.forEach(h -> h.accept(message));
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(
                () -> connection.send(ServerCommand.MESSAGE.formatCommand(getChannelName(), payload)),
                executor
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        handlers.add(consumer);
        connection.registerHandler(getChannelName(), this);
    }

    @Override
    public void close() {
        connection.unregisterHandler(this);
    }

}
