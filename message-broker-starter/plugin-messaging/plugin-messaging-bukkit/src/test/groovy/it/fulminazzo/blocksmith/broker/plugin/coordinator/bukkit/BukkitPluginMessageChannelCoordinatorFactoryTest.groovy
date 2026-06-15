package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class BukkitPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageChannelCoordinatorFactory factory = new BukkitPluginMessageChannelCoordinatorFactory()

    def 'test that create returns BukkitPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(Mock(Plugin))

        then:
        BukkitPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports Plugin'() {
        expect:
        factory.supportsOwner(Plugin)

        and:
        !factory.supportsOwner(Object)
    }

}
