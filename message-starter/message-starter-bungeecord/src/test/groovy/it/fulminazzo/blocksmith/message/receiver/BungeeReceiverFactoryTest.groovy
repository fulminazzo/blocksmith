package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.ServerApplication
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ConnectedPlayer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import net.md_5.bungee.api.plugin.PluginManager
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class BungeeReceiverFactoryTest extends Specification {
    private ProxyServer server
    private ServerApplication application

    void setup() {
        def plugin = Mock(Plugin, additionalInterfaces: [ServerApplication])
        def description = Mock(PluginDescription)
        plugin.description >> description

        server = Mock(ProxyServer)
        server.pluginManager >> Mock(PluginManager)
        server.console >> Mock(CommandSender)
        server.players >> []
        plugin.proxy >> server
        setServer(server)

        application = plugin as ServerApplication
        application.as(_) >> application
        application.server >> server
    }

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def players = [Mock(ProxiedPlayer), Mock(ProxiedPlayer)]
        def console = Mock(CommandSender)

        and:
        def server = Mock(ProxyServer)
        server.players >> players
        server.console >> console
        setServer(server)

        and:
        def factory = new BungeeReceiverFactory().setup(application)

        when:
        def receivers = factory.allReceivers

        then:
        receivers != null

        when:
        def internalReceivers = receivers.collect { it.internal }

        then:
        internalReceivers.sort() == [*players, console].sort()
    }

    def 'test that create does not throw'() {
        given:
        def sender = Mock(CommandSender)

        and:
        def factory = new BungeeReceiverFactory().setup(application)

        when:
        def receiver = factory.create(sender)

        then:
        receiver != null

        and:
        receiver.internal == sender
    }

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType, application)

        then:
        (factory instanceof BungeeReceiverFactory)

        where:
        receiverType << [
                CommandSender,
                ConnectedPlayer,
                ProxiedPlayer
        ]
    }

    static void setServer(final @NotNull ProxyServer server) {
        def field = ProxyServer.getDeclaredField('instance')
        field.accessible = true
        field.set(null, server)
    }

}
