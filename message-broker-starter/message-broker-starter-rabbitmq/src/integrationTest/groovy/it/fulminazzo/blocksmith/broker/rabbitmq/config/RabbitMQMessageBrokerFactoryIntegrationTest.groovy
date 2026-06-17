package it.fulminazzo.blocksmith.broker.rabbitmq.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQIntegrationTest
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBrokerException

class RabbitMQMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest implements RabbitMQIntegrationTest {

    def 'test that build with no port specified works'() {
        when:
        factory.build(config.setPort(0))

        then: 'Exception should be thrown because of a connection refused, not a misconfiguration'
        def e = thrown(RabbitMQMessageBrokerException)
        ConnectException.isInstance(e.cause)
    }

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
