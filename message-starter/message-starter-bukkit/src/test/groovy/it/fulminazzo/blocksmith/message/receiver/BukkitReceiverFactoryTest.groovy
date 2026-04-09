package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.ServerApplication
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

import java.util.logging.Logger

class BukkitReceiverFactoryTest extends Specification {
    private Server server
    private ServerApplication application

    void setup() {
        def plugin = Mock(Plugin, additionalInterfaces: [ServerApplication])
        plugin.description >> {
            def description = Mock(PluginDescriptionFile)
            description.name >> 'Blocksmith'
            return description
        }

        server = Mock(Server)
        server.pluginManager >> Mock(PluginManager)

        application = plugin as ServerApplication
        application.as(_) >> application
        application.server >> server
    }

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def players = [Mock(Player), Mock(Player)]
        def console = Mock(ConsoleCommandSender)

        and:
        server.onlinePlayers >> players
        server.consoleSender >> console
        server.logger >> Logger.anonymousLogger

        Bukkit.server = server

        and:
        def factory = new BukkitReceiverFactory().setup(application)

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
        def factory = new BukkitReceiverFactory().setup(application)

        when:
        def receiver = factory.create(sender)

        then:
        receiver != null

        and:
        receiver.internal == sender
    }

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType)

        then:
        (factory instanceof BukkitReceiverFactory)

        where:
        receiverType << [
                CommandSender,
                ConsoleCommandSender,
                RemoteConsoleCommandSender,
                Player
        ]
    }

}
