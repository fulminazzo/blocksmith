package it.fulminazzo.blocksmith.broker.plugin

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelTest
import it.fulminazzo.blocksmith.broker.Messages
import org.jetbrains.annotations.NotNull

class PluginMessageChannelTest extends MessageChannelTest {
    private static final String channelName = 'plugin-message-channel'

    private final PluginMessageRegistrar registrar = Mock(PluginMessageRegistrar)

    private final Queue<Message> receivedMessages = new LinkedList<>()
    private MockPluginMessageQueryEngine pluginChannel

    void setup() {
        registrar.name >> 'blocksmith'

        pluginChannel = new MockPluginMessageQueryEngine(channelName, registrar)
        pluginChannel.listen { p ->
            logger.debug("Received raw: $p")
            def pair = deserializeMessage(p)
            def msg = pair.first
            logger.info("Received message with id=$msg.id")
            receivedMessages.add(msg)
            if (msg == Messages.MESSAGE1)
                send(Messages.MESSAGE2, pair.second)
        }

        setupChannel()
    }

    @Override
    MessageChannel initializeChannel() {
        return new PluginMessageChannel(
                new MockPluginMessageQueryEngine(
                        channelName,
                        registrar
                ),
                MAPPER
        )
    }

    @Override
    boolean received(final @NotNull Long id) {
        return receivedMessages.any { it.id == id }
    }

    @Override
    void send(final @NotNull Message message, final @NotNull UUID conversationId) {
        pluginChannel.publish(serializeMessage(message, conversationId))
    }

}
