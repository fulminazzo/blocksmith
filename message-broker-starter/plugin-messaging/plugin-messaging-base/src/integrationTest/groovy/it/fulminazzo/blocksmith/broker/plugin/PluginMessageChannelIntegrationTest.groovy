package it.fulminazzo.blocksmith.broker.plugin

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.Messages

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PluginMessageChannelIntegrationTest extends MessageChannelIntegrationTest {
    private final ExecutorService executor = Executors.newCachedThreadPool()

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
        executor?.shutdown()
    }

    def 'test that sending on coordinator works'() {
        when:
        send(message, UUID.randomUUID())

        and:
        sleep(SLEEP_TIME)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        return new PluginMessageChannel(
                new PluginMessageQueryEngine(
                        executor,
                        CHANNEL_NAME,
                        new MockPluginMessageChannelCoordinator()
                ),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new PluginChannelIntegrationTestHelper(channelName, logger)
    }

}
