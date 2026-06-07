package it.fulminazzo.blocksmith.broker.tcp

import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpMessagePeer
import it.fulminazzo.blocksmith.broker.tcp.peer.server.ServerCommand
import org.slf4j.Logger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

@SuppressWarnings('CloseWithoutCloseable')
class TcpChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper {
    static final int DEFAULT_PORT = 40626
    static final long RETRY_INTERVAL = 1_000L

    private final ExecutorService executor
    private final TcpMessagePeer connection

    TcpChannelIntegrationTestHelper(final String channelName, final Logger logger, final int port) {
        super(channelName, logger)
        executor = Executors.newCachedThreadPool()
        connection = new TcpMessagePeer(
                logger,
                MAPPER,
                port,
                RETRY_INTERVAL,
                executor
        )
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        connection.send(ServerCommand.MESSAGE.formatCommand(
                channelName,
                serializeMessage(message, conversationId)
        ))
    }

    @Override
    void close() throws IOException {
        connection?.close()
        executor?.shutdown()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        connection.subscribe(channelName).registerHandler(channelName) { consumer.accept(it) }
        connection.start()
        return this
    }

}
