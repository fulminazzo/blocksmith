package it.fulminazzo.blocksmith.message.receiver

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import net.md_5.bungee.api.plugin.PluginManager
import spock.lang.Specification

class BungeeReceiverTest extends Specification {

    void setupSpec() {
        def plugin = Mock(Plugin)
        def description = Mock(PluginDescription)
        plugin.description >> description
        def server = Mock(ProxyServer)
        server.pluginManager >> Mock(PluginManager)
        server.console >> Mock(CommandSender)
        server.players >> []
        plugin.proxy >> server

        ProxyServer.instance = server

        BungeeReceiver.setup(plugin)
    }

    def 'test that #toAudience converts receiver'() {
        given:
        def receiver = Mock(CommandSender)

        when:
        def actual = new BungeeReceiver(receiver).toAudience()

        then:
        actual != null
    }

    def 'test that getLocale returns player locale'() {
        given:
        def receiver = Mock(ProxiedPlayer)
        receiver.locale >> Locale.ITALY

        when:
        def actual = new BungeeReceiver(receiver).locale

        then:
        actual == Locale.ITALY
    }

    def 'test that getLocale returns system locale for console'() {
        given:
        def receiver = Mock(CommandSender)

        when:
        def actual = new BungeeReceiver(receiver).locale

        then:
        actual == Locale.default
    }

}
