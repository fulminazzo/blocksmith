package it.fulminazzo.blocksmith.broker.rabbitmq.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQIntegrationTest

class RabbitMQMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest implements RabbitMQIntegrationTest {

    @Override
    protected MessageBrokerFactory getFactory() {
        return new RabbitMQMessageBrokerFactory()
    }

    @Override
    protected MessageBrokerConfig getConfig() {
        return new RabbitMQMessageBrokerConfig()
                .setHost(RabbitMQIntegrationTest.serverHost)
                .setPort(RabbitMQIntegrationTest.serverPort)
    }

}
