package it.fulminazzo.blocksmith.broker.kafka.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfigTest
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory

class KafkaMessageBrokerConfigTest extends MessageBrokerConfigTest {

    @Override
    protected Class<? extends MessageBrokerConfig> getConfigType() {
        return KafkaMessageBrokerConfig
    }

    @Override
    protected Class<? extends MessageBrokerFactory> getFactoryType() {
        return KafkaMessageBrokerFactory
    }

}
