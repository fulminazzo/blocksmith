package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import spock.lang.Specification

class BungeecordPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageChannelCoordinatorFactory factory = new BungeecordPluginMessageChannelCoordinatorFactory()

    def 'test that create returns BungeecordPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(Mock(Plugin) { Plugin plugin ->
            plugin.proxy >> Mock(ProxyServer) { it.pluginManager >> Mock(PluginManager) }
        })

        then:
        BungeecordPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports Plugin'() {
        expect:
        factory.supportsOwner(Plugin)

        and:
        !factory.supportsOwner(Object)
    }

}
