package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestUtils
import it.fulminazzo.blocksmith.broker.MessageChannelTest
import it.fulminazzo.blocksmith.broker.Messages
import org.jetbrains.annotations.NotNull
import spock.lang.Shared

class MemoryMessageChannelTest extends MessageChannelTest {
    private static final String CHANNEL_NAME = 'memory-message-channel'

    @Shared
    private MemoryChannelIntegrationTestHelper helper

    void setupSpec() {
        helper = new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                CHANNEL_NAME,
                logger
        ).registerConsumer { m, i ->
            if (m == Messages.MESSAGE1) helper.send(Messages.MESSAGE2, i)
        }
    }

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
        helper.clear()
    }

    def 'test that create function works'() {
        expect:
        MemoryMessageChannel.create(CHANNEL_NAME) != null
    }

    @Override
    MessageChannel initializeChannel() {
        return new MemoryMessageChannel(
                new MemoryMessageQueryEngine(CHANNEL_NAME),
                MessageChannelIntegrationTestUtils.MAPPER
        )
    }

    @Override
    boolean received(final @NotNull Long id) {
        return helper.received(id)
    }

    @Override
    void send(final @NotNull Message message, final @NotNull UUID conversationId) {
        helper.send(message, conversationId)
    }

}
