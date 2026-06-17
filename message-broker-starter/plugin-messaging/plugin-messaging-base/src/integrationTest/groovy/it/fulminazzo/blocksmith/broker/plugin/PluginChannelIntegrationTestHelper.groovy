package it.fulminazzo.blocksmith.broker.plugin

import com.google.common.io.ByteStreams
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.plugin.coordinator.MockPluginMessageChannelCoordinator
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinator
import org.slf4j.Logger

import java.util.function.Consumer

@SuppressWarnings('CloseWithoutCloseable')
class PluginChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper {
    private final PluginMessageChannelCoordinator coordinator

    PluginChannelIntegrationTestHelper(final String channelName, final Logger logger) {
        super(channelName, logger)

        coordinator = new MockPluginMessageChannelCoordinator()
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        def payload = serializeMessage(message, conversationId)
        def stream = ByteStreams.newDataOutput()
        stream.writeUTF(payload)
        coordinator.publish(channelName, stream.toByteArray())
    }

    @Override
    void close() throws IOException {
        coordinator?.close()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        coordinator.registerHandler(
                channelName,
                m -> consumer.accept(m)
        )
        return this
    }

    @SuppressWarnings('UnnecessaryOverridingMethod') // required from Spotbugs
    @Override
    protected final void finalize() throws Throwable {
        super.finalize()
    }

}
