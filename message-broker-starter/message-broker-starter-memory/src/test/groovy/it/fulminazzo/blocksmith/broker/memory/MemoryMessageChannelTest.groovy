package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelTest
import it.fulminazzo.blocksmith.broker.Messages
import org.jetbrains.annotations.NotNull

class MemoryMessageChannelTest extends MessageChannelTest {
    private static final String channelName = 'memory-message-channel'

    private static final Queue<Message> receivedMessages = new LinkedList<>()

    private static final MemoryMessageQueryEngine.MemoryChannel memoryChannel = MemoryMessageQueryEngine.MemoryChannel.getChannel(channelName)

    void setupSpec() {
        memoryChannel.register(
                Mock(MemoryMessageQueryEngine),
                p -> {
                    logger.debug("Received raw: $p")
                    def pair = deserializeMessage(p)
                    def msg = pair.first
                    logger.info("Received message with id=$msg.id")
                    receivedMessages.add(msg)
                    if (msg == Messages.MESSAGE1)
                        send(Messages.MESSAGE2, pair.second)
                }
        )
    }

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
        receivedMessages.clear()
    }

    def 'test that create function works'() {
        expect:
        MemoryMessageChannel.create(channelName) != null
    }

    @Override
    MessageChannel initializeChannel() {
        return new MemoryMessageChannel(
                new MemoryMessageQueryEngine(channelName),
                MAPPER
        )
    }

    @Override
    boolean received(final @NotNull Long id) {
        return receivedMessages.any { it.id == id }
    }

    @Override
    void send(final @NotNull Message message, final @NotNull UUID conversationId) {
        memoryChannel.publish(serializeMessage(message, conversationId))
    }

}
