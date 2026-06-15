package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class BukkitPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageRegistrar validRegistrar = Mock(PluginMessageRegistrar) {
        it.plugin() >> Mock(Plugin)
    }

    private final PluginMessageRegistrar invalidRegistrar = Mock(PluginMessageRegistrar) {
        it.plugin() >> new Object()
        it.server() >> new Object()
    }

    private final PluginMessageChannelCoordinatorFactory factory = new BukkitPluginMessageChannelCoordinatorFactory()

    def 'test that create returns BukkitPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(validRegistrar)

        then:
        BukkitPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports Plugin'() {
        expect:
        factory.supportsRegistrar(validRegistrar)

        and:
        !factory.supportsRegistrar(invalidRegistrar)
    }

}
