package it.fulminazzo.blocksmith.broker.plugin

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

import java.util.concurrent.Executors

@Slf4j
class PluginMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<PluginMessageChannelSettings> {

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<PluginMessageChannelSettings>> newMessageBrokerBuilder() {
        return PluginMessageBroker.builder()
                .owner(new Object())
                .executor(Executors.newCachedThreadPool())
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new PluginChannelIntegrationTestHelper(channelName, log)
    }

    @Override
    protected PluginMessageChannelSettings getSettings() {
        return new PluginMessageChannelSettings()
    }

}
