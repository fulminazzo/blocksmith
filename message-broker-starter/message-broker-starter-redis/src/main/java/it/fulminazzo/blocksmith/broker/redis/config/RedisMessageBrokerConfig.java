package it.fulminazzo.blocksmith.broker.redis.config;

import io.lettuce.core.RedisURI;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.redis.RedisMessageBroker;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * {@link MessageBrokerConfig} for {@link RedisMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see RedisMessageBroker
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class RedisMessageBrokerConfig extends MessageBrokerConfig<RedisMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                RedisMessageBrokerConfig.class,
                new RedisMessageBrokerFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @Nullable
    Integer port = RedisURI.DEFAULT_REDIS_PORT;

    @PositiveOrZero(exceptionMessage = "'database number' must be at least 0")
    @Nullable
    Integer database = 0;

    @Nullable
    String clientName;

    @Nullable
    Boolean ssl = false;

}
