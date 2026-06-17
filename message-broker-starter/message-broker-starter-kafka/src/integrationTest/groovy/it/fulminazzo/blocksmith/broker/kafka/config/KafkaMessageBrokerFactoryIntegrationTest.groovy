package it.fulminazzo.blocksmith.broker.kafka.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest
import it.fulminazzo.blocksmith.broker.kafka.KafkaIntegrationTest

class KafkaMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest implements KafkaIntegrationTest {

    @Override
    protected MessageBrokerFactory getFactory() {
        return new KafkaMessageBrokerFactory()
    }

    @Override
    protected MessageBrokerConfig getConfig() {
        return new KafkaMessageBrokerConfig()
                .setBootstrapServers([
                        new KafkaMessageBrokerConfig.BootstrapServerConfig(
                                KafkaIntegrationTest.serverHost,
                                KafkaIntegrationTest.serverPort
                        )
                ].toSet())
    }

}
