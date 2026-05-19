package it.fulminazzo.blocksmith.broker

import spock.lang.Specification

abstract class MessageBrokerIntegrationTest<S extends MessageChannelSettings<S>> extends Specification {
    private MessageChannelIntegrationTestHelper helper

    void setupSingle() {
        helper = newTestHelper().start()
    }

    void cleanupSingle() {
        helper?.close()
    }

    def 'test broker life cycle'() {
        given:
        def builder = newMessageBrokerBuilder()

        when:
        def broker = builder.build()

        then:
        noExceptionThrown()

        when:
        def messageChannel = broker.newChannel(settings)

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

    protected abstract MessageBrokerBuilder<MessageBroker<S>> newMessageBrokerBuilder()

    protected abstract MessageChannelIntegrationTestHelper newTestHelper()

    protected abstract S getSettings()

}
