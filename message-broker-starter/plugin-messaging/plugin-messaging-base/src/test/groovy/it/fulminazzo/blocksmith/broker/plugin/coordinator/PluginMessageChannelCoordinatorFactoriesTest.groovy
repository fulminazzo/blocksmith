package it.fulminazzo.blocksmith.broker.plugin.coordinator

import spock.lang.Specification

class PluginMessageChannelCoordinatorFactoriesTest extends Specification {

    def 'test that create works for correct owner'() {
        given:
        def owner = new MockOwner()

        when:
        def coordinator = PluginMessageChannelCoordinatorFactories.create(owner)

        then:
        MockPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that create throws for unrecognized owner'() {
        given:
        def owner = new Object()

        when:
        PluginMessageChannelCoordinatorFactories.create(owner)

        then:
        thrown(IllegalArgumentException)
    }

}
