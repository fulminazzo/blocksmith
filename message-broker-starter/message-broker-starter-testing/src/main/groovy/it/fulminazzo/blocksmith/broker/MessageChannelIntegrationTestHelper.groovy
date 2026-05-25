package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import it.fulminazzo.blocksmith.structure.Pair
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

import java.util.function.Consumer

abstract class MessageChannelIntegrationTestHelper implements Closeable {
    static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    private final Queue<Message> receivedMessages = [] as Queue

    protected final String channelName
    protected final Logger logger

    protected MessageChannelIntegrationTestHelper(
            final String channelName,
            final Logger logger
    ) {
        this.channelName = channelName
        this.logger = logger
    }

    abstract void send(final Message message, final UUID conversationId)

    protected abstract MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    )

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    MessageChannelIntegrationTestHelper start() {
        return start(
                channelName,
                logger,
                p -> {
                    logger.debug("Received raw: $p")
                    try {
                        def pair = deserializeMessage(p)

                        def message = pair.first
                        logger.info("Received message with id=$message.id")
                        receivedMessages.add(message)

                        if (message == Messages.MESSAGE1) send(Messages.MESSAGE2, pair.second)
                    } catch (Exception e) {
                        logger.error("Error while parsing message '$p'", e)
                    }
                }
        )
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    boolean received(final Long id) {
        return receivedMessages.any { it.id == id }
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    @Override
    void close() throws IOException {
        receivedMessages.clear()
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static String serializeMessage(final @NotNull Message message, final @NotNull UUID conversationId) {
        return MAPPER.serialize(new AbstractMessageChannel.NetworkMessage(
                UUID.randomUUID(),
                conversationId,
                MAPPER.serialize(message)
        ))
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static Pair<Message, UUID> deserializeMessage(final @NotNull String payload) {
        def actualMessage = MAPPER.deserialize(
                payload,
                AbstractMessageChannel.NetworkMessage
        )
        return Pair.of(
                MAPPER.deserialize(actualMessage.message, Message),
                actualMessage.conversationId
        )
    }

}
