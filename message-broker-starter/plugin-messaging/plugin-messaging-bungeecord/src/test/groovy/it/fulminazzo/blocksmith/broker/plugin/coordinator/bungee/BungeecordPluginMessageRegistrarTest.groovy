package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import spock.lang.Specification

class BungeecordPluginMessageRegistrarTest extends Specification {

    def 'test that registrar returns correct data'() {
        given:
        def server = Mock(ProxyServer)
        def plugin = Mock(Plugin) { it.proxy >> server }

        and:
        def registrar = new BungeecordPluginMessageRegistrar(plugin)

        expect:
        registrar.plugin() == plugin
        registrar.server() == server
    }

}
