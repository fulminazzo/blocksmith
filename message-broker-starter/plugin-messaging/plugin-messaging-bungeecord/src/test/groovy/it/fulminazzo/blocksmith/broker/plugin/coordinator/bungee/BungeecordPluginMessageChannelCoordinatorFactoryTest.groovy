package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import net.md_5.bungee.api.plugin.Plugin
import spock.lang.Specification

class BungeecordPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageRegistrar validRegistrar = Mock(PluginMessageRegistrar) {
        it.plugin() >> Mock(Plugin)
    }

    private final PluginMessageRegistrar invalidRegistrar = Mock(PluginMessageRegistrar) {
        it.plugin() >> new Object()
        it.server() >> new Object()
    }

    private final PluginMessageChannelCoordinatorFactory factory = new BungeecordPluginMessageChannelCoordinatorFactory()

    def 'test that create returns BungeecordPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(validRegistrar)

        then:
        BungeecordPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports Plugin'() {
        expect:
        factory.supportsRegistrar(validRegistrar)

        and:
        !factory.supportsRegistrar(invalidRegistrar)
    }
    
}
