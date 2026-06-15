package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit

import org.bukkit.Server
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class BukkitPluginMessageRegistrarTest extends Specification {

    def 'test that registrar returns correct data'() {
        given:
        def server = Mock(Server)
        def plugin = Mock(Plugin) { it.server >> server }

        and:
        def registrar = new BukkitPluginMessageRegistrar(plugin)

        expect:
        registrar.plugin() == plugin
        registrar.server() == server
    }

}
