package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import it.fulminazzo.blocksmith.structure.Pair
import org.jetbrains.annotations.NotNull

final class MessageChannelIntegrationTestUtils {
    static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    static String serializeMessage(final @NotNull Message message, final @NotNull UUID conversationId) {
        return MAPPER.serialize(new AbstractMessageChannel.NetworkMessage(
                UUID.randomUUID(),
                conversationId,
                MAPPER.serialize(message)
        ))
    }

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
