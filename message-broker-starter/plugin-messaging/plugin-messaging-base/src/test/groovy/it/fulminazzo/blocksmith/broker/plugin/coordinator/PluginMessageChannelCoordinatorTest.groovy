package it.fulminazzo.blocksmith.broker.plugin.coordinator

import com.google.common.io.ByteStreams
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class PluginMessageChannelCoordinatorTest extends Specification {
    private final PluginMessageChannelCoordinator coordinator = Spy(
            PluginMessageChannelCoordinator,
            constructorArgs : [Mock(PluginMessageRegistrar)]
    )

    def 'test that handleIncomingMessage returns true only if handled'() {
        given:
        final channel1 = 'test-channel1'
        final channel2 = 'test-channel2'
        def output = ByteStreams.newDataOutput()
        output.writeUTF('Hello, world!')
        final message = output.toByteArray()

        and:
        Reflect.on(coordinator).get('handlers').invoke('put', channel1, [])

        expect:
        coordinator.handleIncomingMessage(channel1, message)
        !coordinator.handleIncomingMessage(channel2, message)
    }

    def 'test that registerHandler calls on registerChannel if not already present'() {
        given:
        final channel = 'test-channel'

        when:
        coordinator.registerHandler(channel, h -> { })

        then:
        1 * coordinator.registerChannel(channel) >> {}

        when:
        coordinator.registerHandler(channel, h -> { })

        then:
        0 * coordinator.registerChannel(channel) >> {}
    }

    def 'test that unregisterHandler unregisters channel only if no other handler is registered'() {
        given:
        final channel = 'test-channel'

        and:
        PluginMessageHandler handler1 = h -> { }
        PluginMessageHandler handler2 = h -> { }

        and:
        Reflect.on(coordinator).get('handlers').invoke('put', channel, [handler1, handler2])

        when:
        coordinator.unregisterHandler(handler1)

        then:
        0 * coordinator.unregisterChannel(channel) >> {}

        when:
        coordinator.unregisterHandler(handler2)

        then:
        1 * coordinator.unregisterChannel(channel) >> {}
    }

}
