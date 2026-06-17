package it.fulminazzo.blocksmith.broker.plugin.config

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactory
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactoryIntegrationTest

class PluginMessageBrokerFactoryIntegrationTest extends MessageBrokerFactoryIntegrationTest {

    @Override
    protected MessageBrokerFactory getFactory() {
        return new PluginMessageBrokerFactory()
    }

    @Override
    protected MessageBrokerConfig getConfig() {
        return new PluginMessageBrokerConfig().setOwner(new Object())
    }

}
