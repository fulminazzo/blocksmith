package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * Kafka message broker for handling connections and creating Kafka channels.
 * <br>
 * The {@link KafkaMessageChannelSettings#getChannelName()} will be used as <b>topic name</b>.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation (local Kafka):</li>
 *          <pre>{@code
 *          KafkaMessageBroker messageBroker = KafkaMessageBroker.builder()
 *                  .bootstrapServer("localhost", 9002)
 *                  .build();
 *          }</pre>
 *     <li>creation (remote Kafka with authentication):</li>
 *         <pre>{@code
 *         KafkaMessageBroker messageBroker = KafkaMessageBroker.builder()
 *                  .bootstrapServer("localhost", 9002)
 *                  .bootstrapServer("localhost", 9003)
 *                  .bootstrapServer("localhost", 9004)
 *                  // defaults to PLAINTEXT
 *                  .securityProtocol(SecurityProtocol.SSL)
 *                  // defaults to USE_ALL_DNS_IPS
 *                  .clientDnsLookup(ClientDnsLookup.RESOLVE_CANONICAL_BOOTSTRAP_SERVERS_ONLY)
 *                  .build();
 *         }</pre>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         KafkaMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new KafkaMessageChannelSettings()
 *                         .withChannelName("kafka_channel")
 *                         .direct("private_channel")
 *                         .withGroupId("kafka_group_id")
 *                         .withMessagesKey("message_key")
 *                         .idempotenceWithDefaults()
 *                         .enableAutoCommit()
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         KafkaMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomKafkaMessageChannel(engine, mapper),
 *                 new KafkaMessageChannelSettings()
 *                         .withChannelName("kafka_channel")
 *                         .direct("private_channel")
 *                         .withGroupId("kafka_group_id")
 *                         .withMessagesKey("message_key")
 *                         .idempotenceWithDefaults()
 *                         .enableAutoCommit()
 *         );
 *         }</pre>
 *         where CustomKafkaMessageChannel extends {@link KafkaMessageChannel} and adds custom behavior.
 *     </li>
 * </ul>
 *
 * @see KafkaMessageChannel
 * @see KafkaMessageChannelSettings
 * @see KafkaMessageQueryEngine
 */
public final class KafkaMessageBroker extends AbstractMessageBroker<KafkaMessageChannelSettings> {
    private final @NotNull ExecutorService executor;

    private final @NotNull Properties baseProperties;

    /**
     * Instantiates a new Kafka message broker.
     *
     * @param executor       the executor
     * @param baseProperties the base properties to apply to all channels (mainly connection properties)
     * @param mapper         the mapper
     */
    public KafkaMessageBroker(
            final @NotNull ExecutorService executor,
            final @NotNull Properties baseProperties,
            final @NotNull Mapper mapper
    ) {
        super(mapper);
        this.executor = executor;
        this.baseProperties = new Properties();
        this.baseProperties.putAll(baseProperties);
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends KafkaMessageChannel> @NotNull C newChannel(
            final @NotNull BiFunction<KafkaMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull KafkaMessageChannelSettings settings
    ) {
        Properties properties = new Properties();
        properties.putAll(baseProperties);
        properties.putAll(settings.buildProperties());
        // Replace ":" with "." in the topic name, to avoid issues with Kafka
        String topicName = settings.getChannelName().replace(":", ".");
        if (settings.getChannelType() == MessageChannelType.DIRECT)
            topicName += "." + settings.getSubchannelName();
        KafkaMessageQueryEngine queryEngine = new KafkaMessageQueryEngine(
                executor,
                properties,
                topicName,
                settings.getMessagesKey(),
                settings.getAssignmentWaitTime(),
                settings.getPollInterval()
        );
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull KafkaMessageChannelSettings settings) {
        return newChannel(KafkaMessageChannel::new, settings);
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull KafkaMessageBrokerBuilder builder() {
        return new KafkaMessageBrokerBuilder();
    }

}
