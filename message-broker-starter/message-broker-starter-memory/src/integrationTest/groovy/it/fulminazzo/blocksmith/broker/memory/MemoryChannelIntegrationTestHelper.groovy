package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageQueryEngine.MemoryChannel
import org.slf4j.Logger

import java.util.function.Consumer

class MemoryChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper {
    private MemoryMessageQueryEngine queryEngine
    private MemoryChannel memoryChannel

    MemoryChannelIntegrationTestHelper(
            final MemoryMessageQueryEngine queryEngine,
            final String channelName,
            final Logger logger
    ) {
        super(channelName, logger)
        this.queryEngine = queryEngine
    }

    @Override
    MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        memoryChannel = MemoryChannel.getChannel(channelName)
        memoryChannel.register(queryEngine, consumer)
        return this
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        memoryChannel.publish(serializeMessage(message, conversationId))
    }

}
