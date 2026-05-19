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
        def message = messageChannel.sendAndReceiveRaw('hello', 10_000).get()

        then:
        message == 'world'

        when:
        broker.close()

        then:
        noExceptionThrown()
    }

    protected abstract MessageBrokerBuilder<MessageBroker<S>> newMessageBrokerBuilder()

    protected abstract MessageChannelIntegrationTestHelper newTestHelper()

    protected abstract S getSettings()

}
