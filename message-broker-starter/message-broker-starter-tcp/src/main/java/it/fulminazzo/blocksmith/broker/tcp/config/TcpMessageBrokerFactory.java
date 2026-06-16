package it.fulminazzo.blocksmith.broker.tcp.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory;
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageBroker;
import org.jetbrains.annotations.NotNull;

final class TcpMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig<?> config) {
        TcpMessageBrokerConfig tcpConfig = (TcpMessageBrokerConfig) config;
        return TcpMessageBroker.builder()
                .port(tcpConfig.getPort())
                .retryInterval(tcpConfig.getRetryInterval())
                .mapper(config.getMapper())
                .build();
    }

}
