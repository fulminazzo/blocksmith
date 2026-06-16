package it.fulminazzo.blocksmith.broker.tcp.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfigTest
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory

class TcpMessageBrokerConfigTest extends MessageBrokerConfigTest {

    @Override
    protected Class<? extends MessageBrokerConfig> getConfigType() {
        return TcpMessageBrokerConfig
    }

    @Override
    protected Class<? extends MessageBrokerFactory> getFactoryType() {
        return TcpMessageBrokerFactory
    }

}
