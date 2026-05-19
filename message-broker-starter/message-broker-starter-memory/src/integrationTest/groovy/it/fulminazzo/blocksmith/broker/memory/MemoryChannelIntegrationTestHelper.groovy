package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestUtils
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageQueryEngine.MemoryChannel
import org.slf4j.Logger

import java.util.function.BiConsumer

class MemoryChannelIntegrationTestHelper {
    private final Queue<Message> receivedMessages = [] as Queue
    private final List<BiConsumer<Message, UUID>> consumers = []

    private final MemoryChannel memoryChannel

    MemoryChannelIntegrationTestHelper(
            final MemoryMessageQueryEngine queryEngine,
            final String channelName,
            final Logger logger
    ) {
        memoryChannel = MemoryChannel.getChannel(channelName)
        memoryChannel.register(
                queryEngine,
                p -> {
                    logger.debug("Received raw: $p")
                    def pair = MessageChannelIntegrationTestUtils.deserializeMessage(p)

                    def message = pair.first
                    logger.info("Received message with id=$message.id")
                    receivedMessages.add(message)

                    consumers.forEach { it.accept(message, pair.second) }
                }
        )
    }

    MemoryChannelIntegrationTestHelper registerConsumer(final BiConsumer<Message, UUID> consumer) {
        consumers.add(consumer)
        return this
    }

    boolean received(final Long id) {
        return receivedMessages.any { it.id == id }
    }

    void send(final Message message, final UUID conversationId) {
        memoryChannel.publish(MessageChannelIntegrationTestUtils.serializeMessage(message, conversationId))
    }

    void clear() {
        receivedMessages.clear()
    }

}
