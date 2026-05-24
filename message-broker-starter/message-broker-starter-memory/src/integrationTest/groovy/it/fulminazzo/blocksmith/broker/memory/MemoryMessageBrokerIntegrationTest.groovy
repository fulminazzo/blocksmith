package it.fulminazzo.blocksmith.broker.memory

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.broker.Messages
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class MemoryMessageBrokerIntegrationTest extends Specification {
    private static final String CHANNEL_NAME = 'main'
    private static final String SUBCHANNEL_NAME = 'main:sub'

    @Shared
    private MessageChannelIntegrationTestHelper directHelper

    @Shared
    private MessageChannelIntegrationTestHelper broadcastHelper

    void setupSpec() {
        directHelper = new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                "$CHANNEL_NAME:$SUBCHANNEL_NAME",
                log
        ).start()
        broadcastHelper = new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                CHANNEL_NAME,
                log
        ).start()
    }

    void cleanup() {
        broadcastHelper?.close()
        directHelper?.close()
    }

    def 'test broker life cycle'() {
        when:
        def broker = MemoryMessageBroker.create(MapperFormat.JSON.newMapper())

        then:
        noExceptionThrown()

        when:
        def directChannel = broker.newChannel(
                new MemoryMessageChannelSettings()
                        .withChannelName(CHANNEL_NAME)
                        .direct(SUBCHANNEL_NAME)
        )

        then:
        directChannel != null

        when:
        def firstMessage = directChannel.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        firstMessage == Messages.MESSAGE2

        when:
        def broadcastChannel = broker.newChannel(
                new MemoryMessageChannelSettings()
                        .withChannelName(CHANNEL_NAME)
                        .broadcast()
        )

        then:
        broadcastChannel != null

        when:
        def secondMessage = broadcastChannel.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        secondMessage == Messages.MESSAGE2

        when:
        broker.close()

        then:
        noExceptionThrown()
    }

}
