package it.fulminazzo.blocksmith.broker.plugin.coordinator.bukkit

import it.fulminazzo.blocksmith.broker.plugin.coordinator.AbstractPluginMessagePublisher
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import it.fulminazzo.blocksmith.reflect.Reflect
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.messaging.Messenger
import org.mockito.Mockito
import spock.lang.Specification

class BukkitPluginMessageChannelCoordinatorTest extends Specification {
    private static final String CHANNEL_NAME = 'test-channel'

    private final PluginMessageRegistrar registrar = Mock(PluginMessageRegistrar) {
        def messenger = Mock(Messenger)
        def pluginManager = Mock(PluginManager)

        def server = Mock(Server)
        server.messenger >> messenger
        server.pluginManager >> pluginManager

        def plugin = Mock(Plugin)
        plugin.server >> server

        it.server() >> server
        it.plugin() >> plugin
    }

    private final BukkitPluginMessageChannelCoordinator coordinator = new BukkitPluginMessageChannelCoordinator(registrar)

    def 'test that constructor registers listener'() {
        when:
        new BukkitPluginMessageChannelCoordinator(registrar)

        then:
        1 * registrar.server().pluginManager.registerEvents(_ as BukkitPluginMessageChannelCoordinator, registrar.plugin())
    }

    def 'test that coordinator attempts to publish failed messages on player join'() {
        given:
        final message = 'message'.bytes

        and:
        def publisher = Reflect.on(coordinator).get('publisher').get()
        Reflect.on(publisher)
                .get('failedMessages')
                .invoke('put', CHANNEL_NAME, new LinkedList<>([message]))

        and:
        def player = Mock(Player)
        def event = new PlayerJoinEvent(player, 'Player joined!')

        and:
        registrar.server().onlinePlayers >> [player]

        when:
        coordinator.on(event)

        then:
        1 * player.sendPluginMessage(
                registrar.plugin(),
                CHANNEL_NAME,
                message
        )
    }

    def 'test that onPluginMessageReceived delegates to handleIncomingMessage'() {
        given:
        def coordinator = Mock(BukkitPluginMessageChannelCoordinator)
        coordinator.onPluginMessageReceived(*_) >> { callRealMethod() }

        and:
        final message = 'Hello, world!'.bytes

        when:
        coordinator.onPluginMessageReceived(
                CHANNEL_NAME,
                Mock(Player),
                message
        )

        then:
        1 * coordinator.handleIncomingMessage(CHANNEL_NAME, message)
    }

    def 'test that registerChannel correctly registers all necessary channels'() {
        when:
        coordinator.registerChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().messenger.registerIncomingPluginChannel(
                registrar.plugin(),
                CHANNEL_NAME,
                coordinator
        )
        1 * registrar.server().messenger.registerOutgoingPluginChannel(registrar.plugin(), CHANNEL_NAME)
    }

    def 'test that unregisterChannel correctly unregisters all previously registered channels'() {
        when:
        coordinator.unregisterChannel(CHANNEL_NAME)

        then:
        1 * registrar.server().messenger.unregisterIncomingPluginChannel(registrar.plugin(), CHANNEL_NAME)
        1 * registrar.server().messenger.unregisterOutgoingPluginChannel(registrar.plugin(), CHANNEL_NAME)
    }

    def 'test that close unregisters listener'() {
        given:
        def handlerList = Mockito.mockStatic(HandlerList)

        when:
        coordinator.close()

        then:
        handlerList.verify(
                { HandlerList.unregisterAll(coordinator) },
                Mockito.only()
        )

        cleanup:
        handlerList?.close()
    }

    def 'test that BukkitPluginMessageChannelPublisher prioritizes OP players for publishing'() {
        given:
        def nonOp = Mock(Player)
        def op = Mock(Player) { it.op >> true }

        and:
        def publisher = Reflect.on(coordinator).get('publisher').get() as AbstractPluginMessagePublisher

        and:
        def players = [nonOp, op]
        def message = 'Hello, world'.bytes

        and:
        registrar.server().onlinePlayers >> players

        when:
        def result1 = publisher.publishImpl(CHANNEL_NAME, message)

        then:
        result1

        and:
        1 * op.sendPluginMessage(registrar.plugin(), CHANNEL_NAME, message)
        0 * nonOp.sendPluginMessage(registrar.plugin(), CHANNEL_NAME, message)

        when:
        players.remove(op)
        def result2 = publisher.publishImpl(CHANNEL_NAME, message)

        then:
        result2

        and:
        0 * op.sendPluginMessage(registrar.plugin(), CHANNEL_NAME, message)
        1 * nonOp.sendPluginMessage(registrar.plugin(), CHANNEL_NAME, message)

        when:
        players.remove(nonOp)

        then:
        !publisher.publishImpl(CHANNEL_NAME, message)
    }

}
