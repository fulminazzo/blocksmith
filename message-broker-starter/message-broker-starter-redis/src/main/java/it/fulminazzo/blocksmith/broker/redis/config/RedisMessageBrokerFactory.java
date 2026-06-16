package it.fulminazzo.blocksmith.broker.redis.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory;
import it.fulminazzo.blocksmith.broker.redis.RedisMessageBroker;
import org.jetbrains.annotations.NotNull;

final class RedisMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig<?> config) {
        RedisMessageBrokerConfig mbConfig = (RedisMessageBrokerConfig) config;
        return RedisMessageBroker.builder()
                .uri(uri -> {
                    uri.withHost(mbConfig.getHost())
                            .withSsl(Boolean.TRUE.equals(mbConfig.getSsl()));

                    String clientName = mbConfig.getClientName();
                    if (clientName != null) uri.withClientName(clientName);

                    Integer port = mbConfig.getPort();
                    if (port != null) uri.withPort(port);

                    Integer database = mbConfig.getDatabase();
                    if (database != null) uri.withDatabase(database);
                })
                .mapper(mbConfig.getMapper())
                .build();
    }

}
