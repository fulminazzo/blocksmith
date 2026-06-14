package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity

import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelIdentifier
import com.velocitypowered.api.proxy.messages.ChannelRegistrar
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class VelocityPluginMessageChannelCoordinatorTest extends Specification {
    private static final String CHANNEL_NAME = 'test-channel'
    private static final ChannelIdentifier CHANNEL_IDENTIFIER = VelocityChannelUtils.toIdentifier(CHANNEL_NAME)
    private static final List<String> SERVER_NAMES = ['hub', 'factions']

    private final PluginMessageRegistrar registrar = Mock(PluginMessageRegistrar) {
        def servers = SERVER_NAMES.collect { newServer(it) }
        def channelRegistrar = Mock(ChannelRegistrar)
        def eventManager = Mock(EventManager)

        def server = Mock(ProxyServer)
        server.channelRegistrar >> channelRegistrar
        server.eventManager >> eventManager
        server.allServers >> servers
        server.getServer(_) >> { a ->
            Optional.ofNullable(servers.find { it.serverInfo.name == a[0] })
        }

        it.server() >> server
    }

    private final VelocityPluginMessageChannelCoordinator coordinator = new VelocityPluginMessageChannelCoordinator(registrar)

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
        def player = Mock(Player)
        def server = registrar.server().allServers.find { it.serverInfo.name == name }
        def event = new ServerConnectedEvent(player, server, null)

        when:
        coordinator.on(event)

        then:
        1 * server.sendPluginMessage(CHANNEL_IDENTIFIER, message)

        where:
        name << SERVER_NAMES
    }

    def 'test that on PluginMessageEvent delegates to handleIncomingMessage'() {
        given:
        def coordinator = Mock(VelocityPluginMessageChannelCoordinator)
        coordinator.on(_ as PluginMessageEvent) >> { callRealMethod() }

        and:
        final message = 'Hello, world!'.bytes

        when:
        coordinator.on(Mock(PluginMessageEvent) {
            it.identifier >> CHANNEL_IDENTIFIER
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
        server.allServers.forEach {
            1 * it.sendPluginMessage(CHANNEL_IDENTIFIER, message)
        }
    }

    def 'test that registerChannel correctly registers all necessary channels'() {
        when:
        coordinator.registerChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().channelRegistrar.register(CHANNEL_IDENTIFIER)
        1 * registrar.server().eventManager.register(registrar.plugin(), coordinator)
    }

    def 'test that unregisterChannel correctly unregisters all previously registered channels'() {
        when:
        coordinator.unregisterChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().channelRegistrar.unregister(CHANNEL_IDENTIFIER)
        1 * registrar.server().eventManager.unregisterListener(registrar.plugin(), coordinator)
    }

    private RegisteredServer newServer(final String name) {
        return Mock(RegisteredServer) { RegisteredServer server ->
            def serverInfo = Mock(ServerInfo) { it.name >> name }
            server.serverInfo >> serverInfo
        }
    }

}
