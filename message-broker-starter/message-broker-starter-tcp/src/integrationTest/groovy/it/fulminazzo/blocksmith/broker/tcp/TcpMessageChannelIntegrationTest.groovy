package it.fulminazzo.blocksmith.broker.tcp

import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.Messages
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpMessagePeer

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TcpMessageChannelIntegrationTest extends MessageChannelIntegrationTest {
    private ExecutorService executor
    private TcpMessagePeer connection

    void setup() {
        executor = Executors.newCachedThreadPool()
        connection = new TcpMessagePeer(
                logger,
                TcpChannelIntegrationTestHelper.MAPPER,
                TcpChannelIntegrationTestHelper.DEFAULT_PORT,
                TcpChannelIntegrationTestHelper.RETRY_INTERVAL,
                executor
        ).subscribe(CHANNEL_NAME)
        connection.start()
        setupChannel()
    }

    void cleanup() {
        clearData()
        connection?.close()
        executor?.shutdown()
    }

    def 'test that sending on server works'() {
        when:
        send(message, UUID.randomUUID())

        and:
        sleep(SLEEP_TIME)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        return new TcpMessageChannel(
                new TcpMessageQueryEngine(
                        CHANNEL_NAME,
                        connection,
                        executor
                ),
                MessageChannelIntegrationTestHelper.MAPPER
        )
    }

    @Override
    MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new TcpChannelIntegrationTestHelper(channelName, logger, TcpChannelIntegrationTestHelper.DEFAULT_PORT)
    }

}
