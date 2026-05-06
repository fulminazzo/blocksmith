package it.fulminazzo.blocksmith.broker.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import it.fulminazzo.blocksmith.broker.MessageQueryEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A message query engine with Redis support.
 * <br>
 * Uses the <a href="https://lettuce.io">lettuce</a> library under the hood
 * to leverage the speed and optimizations provided by Netty asynchronous operations.
 *
 */
public final class RedisMessageQueryEngine extends MessageQueryEngine {
    private final @NotNull Set<RedisPubSubListener<String, String>> listeners = ConcurrentHashMap.newKeySet();

    private final @NotNull StatefulRedisConnection<String, String> connection;
    private final @NotNull StatefulRedisPubSubConnection<String, String> pubSubConnection;

    /**
     * Instantiates a new Redis message query engine.
     *
     * @param channelName      the channel name
     * @param connection       the connection used for sending
     * @param pubSubConnection the connection used for receiving
     */
    RedisMessageQueryEngine(final @NotNull String channelName,
                            final @NotNull StatefulRedisConnection<String, String> connection,
                            final @NotNull StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        super(channelName);
        this.connection = connection;
        this.pubSubConnection = pubSubConnection;

        pubSubConnection.sync().subscribe(channelName);
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return connection.async().publish(channelName, payload)
                .toCompletableFuture()
                .thenApply(r -> null);
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        RedisPubSubAdapter<String, String> listener = new RedisPubSubAdapter<>() {

            @Override
            public void message(final @NotNull String channel,
                                final @NotNull String message) {
                if (channel.equals(channelName)) consumer.accept(message);
            }

        };
        listeners.add(listener);
        pubSubConnection.addListener(listener);
    }

    @Override
    public void close() {
        listeners.forEach(pubSubConnection::removeListener);
        pubSubConnection.sync().unsubscribe(channelName);
        pubSubConnection.close();
    }

}
