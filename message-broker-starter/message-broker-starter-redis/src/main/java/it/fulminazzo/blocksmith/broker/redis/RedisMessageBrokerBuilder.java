package it.fulminazzo.blocksmith.broker.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import it.fulminazzo.blocksmith.broker.AbstractMessageBrokerBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A builder for {@link RedisMessageBroker}.
 * <br>
 * Example usage:
 * <pre>{@code
 * RedisMessageBroker messageBroker = RedisMessageBroker.builder()
 *         .uri(u -> u
 *                 .withHost("0.0.0.0") // defaults to "127.0.0.1"
 *                 .withPort(6379) // defaults to 6379
 *         )
 *         .clientOptions(c -> c
 *                 .autoReconnect(true)
 *                 .pingBeforeActivateConnection(true)
 *                 .requestQueueSize(4096)
 *         )
 *         .socketOptions(s -> s
 *                 .connectTimeout(Duration.ofSeconds(10))
 *                 .keepAlive(
 *                         SocketOptions.KeepAliveOptions.builder()
 *                                 .idle(Duration.ofSeconds(5))
 *                                 .interval(Duration.ofSeconds(5))
 *                                 .count(3)
 *                                 .enable()
 *                                 .build()
 *                 )
 *         )
 *         .mapper(MapperFormat.SERIALIZABLE.newMapper()) // defaults to JSON
 *         .build();
 * }</pre>
 */
public final class RedisMessageBrokerBuilder extends AbstractMessageBrokerBuilder<RedisMessageBroker, RedisMessageBrokerBuilder> {
    private final @NotNull ClientOptions.Builder clientOptions = ClientOptions.builder();
    private final @NotNull SocketOptions.Builder socketOptions = SocketOptions.builder();

    private final @NotNull RedisURI.Builder redisURIbuilder;

    /**
     * Instantiates a new Redis message broker builder.
     */
    RedisMessageBrokerBuilder() {
        this.redisURIbuilder = RedisURI.builder(RedisURI.create("redis://127.0.0.1:6379/0"));
    }

    @Override
    public @NotNull RedisMessageBroker build() {
        final RedisClient client = RedisClient.create(redisURIbuilder.build());
        client.setOptions(clientOptions
                .socketOptions(socketOptions.build())
                .build()
        );
        return new RedisMessageBroker(client, mapper);
    }

    /**
     * Allows editing the internal redis URI using the lettuce provided builder.
     *
     * @param editFunction the function to edit the URI
     * @return this object (for method chaining)
     */
    public @NotNull RedisMessageBrokerBuilder uri(final @NotNull Consumer<RedisURI.Builder> editFunction) {
        editFunction.accept(redisURIbuilder);
        return this;
    }

    /**
     * Applies the given function to the client options (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull RedisMessageBrokerBuilder clientOptions(final @NotNull Consumer<ClientOptions.Builder> function) {
        function.accept(clientOptions);
        return this;
    }

    /**
     * Applies the given function to the socket options (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull RedisMessageBrokerBuilder socketOptions(final @NotNull Consumer<SocketOptions.Builder> function) {
        function.accept(socketOptions);
        return this;
    }

}
