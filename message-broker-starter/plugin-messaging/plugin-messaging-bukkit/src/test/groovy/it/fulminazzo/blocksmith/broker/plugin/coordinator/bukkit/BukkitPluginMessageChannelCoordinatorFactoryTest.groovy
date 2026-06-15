package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

class BukkitPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageChannelCoordinatorFactory factory = new BukkitPluginMessageChannelCoordinatorFactory()

    def 'test that create returns BukkitPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(Mock(Plugin) { Plugin plugin ->
            plugin.server >> Mock(Server) { it.pluginManager >> Mock(PluginManager) }
        })

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
