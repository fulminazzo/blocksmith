package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest

class MemoryMessageChannelIntegrationTest extends MessageChannelIntegrationTest {

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
    }

    def 'test that create function works'() {
        expect:
        MemoryMessageChannel.create(CHANNEL_NAME) != null
    }

    @Override
    MessageChannel initializeChannel() {
        return new MemoryMessageChannel(
                new MemoryMessageQueryEngine(CHANNEL_NAME),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                channelName,
                logger
        )
    }

}
