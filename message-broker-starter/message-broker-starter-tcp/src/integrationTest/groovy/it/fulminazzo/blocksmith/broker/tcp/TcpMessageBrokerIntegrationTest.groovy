package it.fulminazzo.blocksmith.broker.tcp

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class TcpMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<TcpMessageChannelSettings> {
    private static final int PORT = 40627

    private ExecutorService executor

    void setup() {
        executor = Executors.newCachedThreadPool()
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
        executor?.shutdown()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<TcpMessageChannelSettings>> newMessageBrokerBuilder() {
        return TcpMessageBroker.builder()
                .port(PORT)
                .retryInterval(5_000L)
                .logger(log)
                .executor(executor)
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new TcpChannelIntegrationTestHelper(channelName, log, PORT)
    }

    @Override
    protected TcpMessageChannelSettings getSettings() {
        return new TcpMessageChannelSettings()
    }

}
