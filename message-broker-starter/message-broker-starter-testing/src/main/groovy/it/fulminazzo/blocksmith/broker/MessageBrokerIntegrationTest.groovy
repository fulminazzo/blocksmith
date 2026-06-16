package it.fulminazzo.blocksmith.broker

import spock.lang.Specification

abstract class MessageBrokerIntegrationTest<S extends MessageChannelSettings> extends Specification {
    private static final String CHANNEL_NAME = 'message-broker-integration-test'
    private static final String SUBCHANNEL_NAME = 'direct'
    private static final String DIRECT_CHANNEL_NAME = "$CHANNEL_NAME-direct"
    private static final String BROADCAST_CHANNEL_NAME = "$CHANNEL_NAME-broadcast"

    private MessageChannelIntegrationTestHelper directHelper
    private MessageChannelIntegrationTestHelper broadcastHelper

    void setupSingle() {
        directHelper = newTestHelper("$DIRECT_CHANNEL_NAME:$SUBCHANNEL_NAME").start()
        broadcastHelper = newTestHelper(BROADCAST_CHANNEL_NAME).start()
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
        def direct = broker.newChannel(settings.withChannelName(DIRECT_CHANNEL_NAME).direct(SUBCHANNEL_NAME))

        then:
        direct != null

        when:
        def firstMessage = direct.sendAndReceive(Messages.MESSAGE1, Message, 10_000).get()

        then:
        firstMessage == Messages.MESSAGE2

        when:
        def broadcast = broker.newChannel(settings.withChannelName(BROADCAST_CHANNEL_NAME).broadcast())

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
