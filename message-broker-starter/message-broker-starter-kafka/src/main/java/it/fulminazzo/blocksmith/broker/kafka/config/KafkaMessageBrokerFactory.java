package it.fulminazzo.blocksmith.broker.kafka.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory;
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageBroker;
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageBrokerBuilder;
import org.jetbrains.annotations.NotNull;

final class KafkaMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig<?> config) {
        KafkaMessageBrokerConfig mbConfig = (KafkaMessageBrokerConfig) config;

        KafkaMessageBrokerBuilder builder = KafkaMessageBroker.builder()
                .securityProtocol(mbConfig.getSecurityProtocol())
                .clientDnsLookup(mbConfig.getClientDnsLookup())
                .reconnectBackoff(mbConfig.getReconnectBackoff())
                .reconnectBackoffMax(mbConfig.getReconnectBackoffMax())
                .requestTimeout(mbConfig.getRequestTimeout())
                .mapper(mbConfig.getMapper());

        mbConfig.getBootstrapServers().forEach(s ->
                builder.bootstrapServer(s.getHost(), s.getPort())
        );

        return builder.build();
    }

}
