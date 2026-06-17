package it.fulminazzo.blocksmith.broker.tcp.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest

class TcpMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest {
    private static final int PORT = 40628

    @Override
    protected MessageBrokerFactory getFactory() {
        return new TcpMessageBrokerFactory()
    }

    @Override
    protected MessageBrokerConfig getConfig() {
        return new TcpMessageBrokerConfig().setPort(PORT)
    }

}
