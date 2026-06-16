package it.fulminazzo.blocksmith.broker.plugin.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfigTest
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory

class PluginMessageBrokerConfigTest extends MessageBrokerConfigTest {

    @Override
    protected Class<? extends MessageBrokerConfig> getConfigType() {
        return PluginMessageBrokerConfig
    }

    @Override
    protected Class<? extends MessageBrokerFactory> getFactoryType() {
        return PluginMessageBrokerFactory
    }

}
