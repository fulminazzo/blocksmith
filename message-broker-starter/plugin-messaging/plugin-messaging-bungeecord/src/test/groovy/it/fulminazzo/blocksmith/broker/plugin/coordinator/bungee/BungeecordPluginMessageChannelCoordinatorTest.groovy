package it.fulminazzo.blocksmith.broker.plugin.coordinator.bungee

import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import it.fulminazzo.blocksmith.reflect.Reflect
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.PluginManager
import spock.lang.Specification

class BungeecordPluginMessageChannelCoordinatorTest extends Specification {
    private static final String CHANNEL_NAME = 'test-channel'
    private static final List<String> SERVER_NAMES = ['hub', 'factions']

    private final PluginMessageRegistrar registrar = Mock(PluginMessageRegistrar) {
        def servers = SERVER_NAMES.collectEntries { [it, newServer(it)] }
        def pluginManager = Mock(PluginManager)

        def server = Mock(ProxyServer)
        server.pluginManager >> pluginManager
        server.servers >> servers
        server.getServerInfo(_) >> { a -> servers[a[0]] }

        it.server() >> server
    }

    private final BungeecordPluginMessageChannelCoordinator coordinator = new BungeecordPluginMessageChannelCoordinator(registrar)

    def 'test that constructor registers listener'() {
        when:
        new BungeecordPluginMessageChannelCoordinator(registrar)

        then:
        1 * registrar.server().pluginManager.registerListener(registrar.plugin(), _ as BungeecordPluginMessageChannelCoordinator)
    }

    def 'test that coordinator attempts to publish failed messages on player connect to server'() {
        given:
        final message = 'message'.bytes

        and:
        SERVER_NAMES.forEach {
            def publisher = coordinator.getNodePublisher(it)
            Reflect.on(publisher)
                    .get('failedMessages')
                    .invoke('put', CHANNEL_NAME, new LinkedList<>([message]))
        }

        and:
        def player = Mock(ProxiedPlayer)
        def server = Mock(Server) {
            it.info >> registrar.server().servers[name]
        }
        def event = new ServerConnectedEvent(player, server)

        when:
        coordinator.on(event)

        then:
        1 * registrar.server().servers[name].sendData(CHANNEL_NAME, message, false)

        where:
        name << SERVER_NAMES
    }

    def 'test that on PluginMessageEvent delegates to handleIncomingMessage'() {
        given:
        def coordinator = Mock(BungeecordPluginMessageChannelCoordinator)
        coordinator.on(_ as PluginMessageEvent) >> { callRealMethod() }

        and:
        final message = 'Hello, world!'.bytes

        when:
        coordinator.on(Mock(PluginMessageEvent) {
            it.tag >> CHANNEL_NAME
            it.data >> message
        })

        then:
        1 * coordinator.handleIncomingMessage(CHANNEL_NAME, message)
    }

    def 'test that publish calls on all nodes'() {
        given:
        def message = 'Hello, world'.bytes

        and:
        def server = registrar.server() as ProxyServer

        when:
        coordinator.publish(CHANNEL_NAME, message)

        then:
        1 * server.servers['hub'].sendData(CHANNEL_NAME, message, false)
        1 * server.servers['factions'].sendData(CHANNEL_NAME, message, false)
    }

    def 'test that registerChannel correctly registers all necessary channels'() {
        when:
        coordinator.registerChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().registerChannel(CHANNEL_NAME)
    }

    def 'test that unregisterChannel correctly unregisters all previously registered channels'() {
        when:
        coordinator.unregisterChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().unregisterChannel(CHANNEL_NAME)
    }

    def 'test that close correctly unregisters listener'() {
        when:
        coordinator.close()

        then:
        1 * registrar.server().pluginManager.unregisterListener(coordinator)
    }

    protected ServerInfo newServer(final String name) {
        return Mock(ServerInfo) {
            it.name >> name
        }
    }

}
