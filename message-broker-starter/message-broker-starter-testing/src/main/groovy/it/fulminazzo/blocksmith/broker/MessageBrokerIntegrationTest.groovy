package it.fulminazzo.blocksmith.broker

import spock.lang.Specification

abstract class MessageBrokerIntegrationTest<S extends MessageChannelSettings<S>> extends Specification {
    protected static final String CHANNEL_NAME = 'message-broker-integration-test'
    protected static final String SUBCHANNEL_NAME = 'direct'

    private MessageChannelIntegrationTestHelper directHelper
    private MessageChannelIntegrationTestHelper broadcastHelper

    void setupSingle() {
        directHelper = newTestHelper("$CHANNEL_NAME:$SUBCHANNEL_NAME").start()
        broadcastHelper = newTestHelper(CHANNEL_NAME).start()
    }

    void cleanupSingle() {
        broadcastHelper?.close()
        directHelper?.close()
    }

    def 'test broker life cycle'() {
        given:
        def builder = newMessageBrokerBuilder()

        when:
        def broker = builder.build()

        then:
        noExceptionThrown()

        when:
        def direct = broker.newChannel(settings.direct(SUBCHANNEL_NAME))

        then:
        direct != null

        when:
        def firstMessage = direct.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        firstMessage == Messages.MESSAGE2

        when:
        def broadcast = broker.newChannel(settings.broadcast())

        then:
        broadcast != null

        when:
        def secondMessage = broadcast.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        secondMessage == Messages.MESSAGE2

        when:
        broker.close()

        then:
        noExceptionThrown()
    }

    protected abstract MessageBrokerBuilder<MessageBroker<S>> newMessageBrokerBuilder()

    protected abstract MessageChannelIntegrationTestHelper newTestHelper(final String channelName)

    protected abstract S getSettings()

}
