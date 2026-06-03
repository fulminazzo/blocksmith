package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpMessagePeer;
import it.fulminazzo.blocksmith.broker.tcp.peer.server.ServerCommand;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * A special message broker that instantiates connections to a custom TCP protocol.
 * <br>
 * The protocol uses a <b>peer-based</b> connection system, where each node tries to start its own server.
 * If it succeeds, each client will then connect to it until it restarts, at which point another client
 * will take its place.
 * <br>
 * The protocol supports very basic commands to ensure a simple yet fast interface for testing purposes mainly.
 * Check {@link ServerCommand} for more information.
 * <br>
 * Examples:
 * <ul>
 *     //TODO: creation examples
 *     <li>creating a standard channel:
 *     <pre>{@code
 *     TcpMessageBroker messageBroker = ...;
 *     MessageChannel channel = messageBroker.newChannel(
 *             new TcpMessageChannelSettings()
 *                     .withChannelName("tcp_channel")
 *                     .direct("private_channel")
 *     );
 *     }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *     <pre>{@code
 *     TcpMessageBroker messageBroker = ...;
 *     MessageChannel channel = messageBroker.newChannel(
 *             (engine, mapper) -> new CustomTcpMessageChannel(engine, mapper),
 *             new TcpMessageChannelSettings()
 *                     .withChannelName("tcp_channel")
 *                     .direct("private_channel")
 *     );
 *     }</pre>
 *     where CustomTcpMessageChannel extends {@link TcpMessageChannel} and adds custom behavior.
 *     </li>
 * </ul>
 *
 * @see TcpMessagePeer
 * @see ServerCommand
 * @see TcpMessageChannel
 * @see TcpMessageChannelSettings
 * @see TcpMessageQueryEngine
 */
public final class TcpMessageBroker extends AbstractMessageBroker<TcpMessageChannelSettings> {
    private final @NotNull TcpMessagePeer connection;
    private final @NotNull ExecutorService executor;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new Tcp message broker.
     *
     * @param port          the port to connect on
     * @param retryInterval the time to wait before trying to reconnect to the server (in milliseconds)
     * @param mapper        the mapper
     * @param logger        the logger used to display messages
     * @param executor      the executor to handle internal tasks with
     */
    TcpMessageBroker(
            final int port,
            final long retryInterval,
            final @NotNull Mapper mapper,
            final @NotNull Logger logger,
            final @NotNull ExecutorService executor
    ) {
        this.connection = new TcpMessagePeer(
                logger,
                mapper,
                port,
                retryInterval,
                executor
        );
        this.mapper = mapper;
        this.executor = executor;
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends TcpMessageChannel> @NotNull C newChannel(
            final @NotNull BiFunction<TcpMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull TcpMessageChannelSettings settings
    ) {
        String channelName = settings.getChannelName();
        if (settings.getChannelType() == MessageChannelType.DIRECT)
            channelName += ":" + settings.getSubchannelName();
        TcpMessageQueryEngine queryEngine = new TcpMessageQueryEngine(
                channelName,
                connection,
                executor
        );
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull TcpMessageChannelSettings settings) {
        return newChannel(TcpMessageChannel::new, settings);
    }

    @Override
    public void close() throws IOException {
        super.close();
        connection.close();
        executor.shutdown();
    }

    //TODO: re-enable
//    /**
//     * Gets a new builder for this class.
//     *
//     * @return the builder
//     */
//    public static @NotNull TcpMessageBrokerBuilder builder() {
//        return new TcpMessageBrokerBuilder();
//    }

}
