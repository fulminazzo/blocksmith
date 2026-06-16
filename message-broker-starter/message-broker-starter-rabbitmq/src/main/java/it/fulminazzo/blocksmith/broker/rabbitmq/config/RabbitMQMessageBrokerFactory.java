package it.fulminazzo.blocksmith.broker.rabbitmq.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory;
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBroker;
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBrokerBuilder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
final class RabbitMQMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig<?> config) {
        RabbitMQMessageBrokerConfig mbConfig = (RabbitMQMessageBrokerConfig) config;

        RabbitMQMessageBrokerBuilder builder = RabbitMQMessageBroker.builder()
                .host(mbConfig.getHost())
                .username(mbConfig.getUsername())
                .password(mbConfig.getPassword())
                .mapper(mbConfig.getMapper());

        Integer port = mbConfig.getPort();
        if (port != 0) builder.port(port);

        return builder.build();
    }

}
