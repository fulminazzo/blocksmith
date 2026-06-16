package it.fulminazzo.blocksmith.broker.rabbitmq.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfigTest
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory

class RabbitMQMessageBrokerConfigTest extends MessageBrokerConfigTest {

    @Override
    protected Class<? extends MessageBrokerConfig> getConfigType() {
        return RabbitMQMessageBrokerConfig
    }

    @Override
    protected Class<? extends MessageBrokerFactory> getFactoryType() {
        return RabbitMQMessageBrokerFactory
    }

}
