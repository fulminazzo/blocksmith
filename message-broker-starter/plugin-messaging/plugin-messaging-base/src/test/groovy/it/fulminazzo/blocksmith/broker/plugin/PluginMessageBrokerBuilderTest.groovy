package it.fulminazzo.blocksmith.broker.plugin

import it.fulminazzo.blocksmith.broker.plugin.coordinator.MockOwner
import spock.lang.Specification

class PluginMessageBrokerBuilderTest extends Specification {

    def 'test that build with no specified executor does not throw'() {
        given:
        def builder = new PluginMessageBrokerBuilder().owner(new MockOwner())

        when:
        def broker = builder.build()

        then:
        broker.executor != null

        cleanup:
        broker?.close()
    }

}
