package it.fulminazzo.blocksmith.broker.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import it.fulminazzo.blocksmith.broker.AbstractMessageBroker;
import it.fulminazzo.blocksmith.broker.MessageChannel;
import it.fulminazzo.blocksmith.broker.MessageChannelType;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * Redis message broker for handling connections and creating Redis channels.
 * <br>
 * Manages Redis client connections and creates channels for sending and receiving messages
 * in Redis using the Lettuce reactive client.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation (local Redis):
 *         <pre>{@code
 *         RedisMessageBroker messageBroker = RedisMessageBroker.builder()
 *                 // defaults to "127.0.0.1:6379"
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creation (remote Redis with authentication):
 *         <pre>{@code
 *         RedisMessageBroker messageBroker = RedisMessageBroker.builder()
 *                 .uri(u -> u
 *                         .withHost("0.0.0.0")
 *                         .withPort(6379)
 *                         .withPassword("SuperSecurePassword")
 *                         .withSsl(true)
 *                 )
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a standard channel:
 *         <pre>{@code
 *         RedisMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 new RedisMessageChannelSettings()
 *                         .withChannelName("redis_channel")
 *                         .direct("private_channel")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom channel:
 *         <pre>{@code
 *         RedisMessageBroker messageBroker = ...;
 *         MessageChannel channel = messageBroker.newChannel(
 *                 (engine, mapper) -> new CustomRedisMessageChannel(engine, mapper),
 *                 new RedisMessageChannelSettings()
 *                         .withChannelName("redis_channel")
 *                         .direct("private_channel")
 *         );
 *         }</pre>
 *         where CustomRedisMessageChannel extends RedisMessageChannel and adds custom behavior.
 *     </li>
 * </ul>
 */
public final class RedisMessageBroker extends AbstractMessageBroker<RedisMessageChannelSettings> {
    private final @NotNull RedisClient redisClient;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new Redis message broker.
     *
     * @param redisClient the redis client
     * @param mapper      the mapper
     */
    RedisMessageBroker(final @NotNull RedisClient redisClient,
                       final @NotNull Mapper mapper) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.mapper = mapper;
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NotNull RedisMessageChannelSettings settings) {
        return newChannel(RedisMessageChannel::new, settings);
    }

    /**
     * Creates a new custom channel.
     *
     * @param <C>            the type of the channel
     * @param channelBuilder the channel creation function
     * @param settings       the settings to build the channel with
     * @return the channel
     */
    public <C extends RedisMessageChannel> @NotNull C newChannel(
            final @NotNull BiFunction<RedisMessageQueryEngine, Mapper, C> channelBuilder,
            final @NotNull RedisMessageChannelSettings settings
    ) {
        String channelName = settings.getChannelName();
        if (settings.getChannelType() == MessageChannelType.DIRECT)
            channelName += ":" + settings.getSubchannelName();
        RedisMessageQueryEngine queryEngine = new RedisMessageQueryEngine(
                connection,
                redisClient.connectPubSub(),
                channelName
        );
        return registerChannel(channelBuilder.apply(queryEngine, mapper));
    }

    @Override
    public void close() throws IOException {
        super.close();
        connection.close();
        redisClient.shutdown();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull RedisMessageBrokerBuilder builder() {
        return new RedisMessageBrokerBuilder();
    }

}
