package it.fulminazzo.blocksmith.broker.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * RabbitMQ message broker for handling connections and creating RabbitMQ channels.
 * <br>
 * The channels are configured with the following rules:
 * <ul>
 *     <li>the {@link RabbitMQMessageChannelSettings#getChannelName()} is the <b>exchange name</b>;</li>
 *     <li>if the {@link RabbitMQMessageChannelSettings#getChannelType()} is {@link MessageChannelType#DIRECT},
 *     the {@link RabbitMQMessageChannelSettings#getSubchannelName()} is used as <b>routing key</b>;</li>
 *     <li><b>consumer tags</b> are programmatically generated;</li>
 *     <li>the <b>queue name</b> is configured from the user.</li>
 * </ul>
 * Examples:
 * <ul>
 *     <li>creation (local RabbitMQ):
 *         <pre>{@code
 *         RabbitMQMessageBroker messageBroker = RabbitMQMessageBroker.builder()
 *                 // defaults to "amqp://guest:guest@127.0.0.1:5672"
 *                 .build()
 *         }</pre>
 *     </li>
 *     <li>creation (remote RabbitMQ with authentication):
 *         <pre>{@code
 *         RabbitMQMessageBroker messageBroker = RabbitMQMessageBroker.builder()
 *                 .host("0.0.0.0")
 *                 .port(5671)
 *                 .username("root")
 *                 .password("super-secure-password-should-use-an-env-variable")
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         RabbitMQMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new RabbitMQMessageChannelSettings()
 *                         .withChannelName("rabbitmq_channel")
 *                         .direct("private_channel")
 *                         .withQueueName("rabbitmq_queue")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         RabbitMQMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomRabbitMQMessageChannel(engine, mapper),
 *                 new RabbitMQMessageChannelSettings()
 *                         .withChannelName("rabbitmq_channel")
 *                         .direct("private_channel")
 *                         .withQueueName("rabbitmq_queue")
 *         );
 *         }</pre>
 *         where CustomRabbitMQMessageChannel extends {@link RabbitMQMessageChannel} and adds custom behavior.
 *     </li>
 * </ul>
 *
 * @see RabbitMQMessageChannel
 * @see RabbitMQMessageChannelSettings
 * @see RabbitMQMessageQueryEngine
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class RabbitMQMessageBroker extends AbstractMessageBroker<RabbitMQMessageChannelSettings> {
    private final @NotNull ExecutorService executor;

    private final @NotNull Connection connection;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new RabbitMQ message broker.
     *
     * @param executor   the executor
     * @param connection the connection
     * @param mapper     the mapper
     */
    RabbitMQMessageBroker(
            final @NotNull ExecutorService executor,
            final @NotNull Connection connection,
            final @NotNull Mapper mapper
    ) {
        this.executor = executor;
        this.connection = connection;
        this.mapper = mapper;
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends RabbitMQMessageChannel> C newChannel(
            final @NotNull BiFunction<RabbitMQMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull RabbitMQMessageChannelSettings settings
    ) {
        try {
            String exchangeName = settings.getChannelName();
            String subchannelName = settings.getSubchannelNameOrNull();
            if (subchannelName != null) exchangeName += "." + subchannelName;
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(
                    exchangeName,
                    settings.getChannelType() == MessageChannelType.DIRECT ? "direct" : "fanout",
                    true
            );
            RabbitMQMessageQueryEngine queryEngine = new RabbitMQMessageQueryEngine(
                    executor,
                    exchangeName,
                    channel,
                    subchannelName,
                    settings.getQueueSettings()
            );
            return registerChannel(channelBuilder.apply(queryEngine, mapper));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull RabbitMQMessageChannelSettings settings) {
        return newChannel(RabbitMQMessageChannel::new, settings);
    }

    @Override
    public void close() throws IOException {
        super.close();
        connection.close();
        executor.shutdown();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull RabbitMQMessageBrokerBuilder builder() {
        return new RabbitMQMessageBrokerBuilder();
    }

}
