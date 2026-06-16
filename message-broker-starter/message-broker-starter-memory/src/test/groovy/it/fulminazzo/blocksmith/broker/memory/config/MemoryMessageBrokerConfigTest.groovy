package it.fulminazzo.blocksmith.broker.memory.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfigTest
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory

class MemoryMessageBrokerConfigTest extends MessageBrokerConfigTest {

    @Override
    protected Class<? extends MessageBrokerConfig> getConfigType() {
        return MemoryMessageBrokerConfig
    }

    @Override
    protected Class<? extends MessageBrokerFactory> getFactoryType() {
        return MessageBrokerFactory
    }

}
