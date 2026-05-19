package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.MessageChannelTest

class MemoryMessageChannelTest extends MessageChannelTest {
    private static final String CHANNEL_NAME = 'memory-message-channel'

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
    MessageChannelIntegrationTestHelper newTestHelper() {
        return new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                CHANNEL_NAME,
                logger
        )
    }

}
