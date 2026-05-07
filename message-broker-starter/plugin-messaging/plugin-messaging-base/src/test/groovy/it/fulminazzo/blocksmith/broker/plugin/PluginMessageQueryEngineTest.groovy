package it.fulminazzo.blocksmith.broker.plugin

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class PluginMessageQueryEngineTest extends Specification {
    private PluginMessageQueryEngine engine = Spy(MockPluginMessageQueryEngine,
            constructorArgs: ['query-engine-test', Mock(PluginMessageRegistrar)]
    )

    private Queue<byte[]> pendingMessages = Reflect.on(engine).get('pendingMessages').get()

    def 'test that publish stores to pending messages on failed send'() {
        given:
        engine.publish(_ as byte[]) >> false

        and:
        final message = 'Hello, world!'

        when:
        engine.publish(message).join()

        then:
        pendingMessages.size() == 1
        message == new String(pendingMessages[0])
    }

    def 'test that resendPendingMessages works'() {
        given:
        def messages = ['Hello'.bytes, 'world'.bytes]

        and:
        messages.each { pendingMessages.add(it) }

        and:
        engine.publish(_ as byte[]) >> true

        when:
        engine.resendPendingMessages()

        then:
        pendingMessages.empty

        and:
        1 * engine.publish(messages[0])
        1 * engine.publish(messages[1])
    }

    def 'test that resendPendingMessages stops on first error and re-adds to queue'() {
        given:
        def messages = ['Hello'.bytes, 'world'.bytes]

        and:
        messages.each { pendingMessages.add(it) }

        and:
        def engine = Mock(MockPluginMessageQueryEngine)

        and:
        engine.publish(_ as byte[]) >> false
        engine.resendPendingMessages() >> {
            callRealMethod()
        }

        when:
        engine.resendPendingMessages()

        then:
        pendingMessages.size() == 2
        new String(pendingMessages[0]) == new String(messages[0])
        new String(pendingMessages[1]) == new String(messages[1])

        and:
        1 * engine.publish(messages[0])
    }

}
