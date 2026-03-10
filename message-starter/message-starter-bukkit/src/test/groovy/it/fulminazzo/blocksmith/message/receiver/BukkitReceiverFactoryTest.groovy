package it.fulminazzo.blocksmith.message.receiver

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Player
import org.mockito.MockedStatic
import org.mockito.Mockito
import spock.lang.Specification

import java.util.logging.Logger

class BukkitReceiverFactoryTest extends Specification {

    private static MockedStatic<?> mock

    void setupSpec() {
        def adventure = Mock(BukkitAudiences)
        adventure.sender(_) >> Mock(Audience)

        mock = Mockito.mockStatic(BukkitReceiverFactory)
        mock.when(BukkitReceiverFactory::getAdventure).thenReturn(adventure)
    }

    void cleanupSpec() {
        mock?.close()
    }

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def players = [Mock(Player), Mock(Player)]
        def console = Mock(ConsoleCommandSender)

        and:
        def server = Mock(Server)
        server.onlinePlayers >> players
        server.consoleSender >> console
        server.logger >> Logger.anonymousLogger

        Bukkit.server = server

        and:
        def factory = new BukkitReceiverFactory()

        when:
        def receivers = factory.allReceivers

        then:
        receivers != null

        when:
        def internalReceivers = receivers.collect { it.receiver }

        then:
        internalReceivers.sort() == [*players, console].sort()
    }

    def 'test that create does not throw'() {
        given:
        def sender = Mock(CommandSender)

        and:
        def factory = new BukkitReceiverFactory()

        when:
        def receiver = factory.create(sender)

        then:
        receiver != null

        and:
        receiver.receiver == sender
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
