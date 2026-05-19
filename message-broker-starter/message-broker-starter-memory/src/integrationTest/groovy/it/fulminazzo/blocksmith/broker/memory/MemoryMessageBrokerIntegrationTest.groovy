package it.fulminazzo.blocksmith.broker.memory

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.Messages
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class MemoryMessageBrokerIntegrationTest extends Specification {
    private static final String CHANNEL_NAME = 'main:sub'

    @Shared
    private MemoryChannelIntegrationTestHelper helper

    void setupSpec() {
        helper = new MemoryChannelIntegrationTestHelper(
                Mock(MemoryMessageQueryEngine),
                CHANNEL_NAME,
                log
        ).registerConsumer { m, i ->
            if (m == Messages.MESSAGE1) helper.send(Messages.MESSAGE2, i)
        }
    }

    void cleanup() {
        helper.clear()
    }

    def 'test broker life cycle'() {
        when:
        def broker = MemoryMessageBroker.create(MapperFormat.JSON.newMapper())

        then:
        noExceptionThrown()

        when:
        def messageChannel = broker.newChannel(
                new MemoryMessageChannelSettings()
                        .withChannelName('main')
                        .direct('sub')
        )

        then:
        messageChannel != null

        when:
        def message = messageChannel.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        message == Messages.MESSAGE2

        when:
        broker.close()

        then:
        noExceptionThrown()
    }

}
