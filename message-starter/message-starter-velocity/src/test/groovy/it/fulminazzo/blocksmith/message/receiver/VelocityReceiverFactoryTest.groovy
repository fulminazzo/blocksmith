package it.fulminazzo.blocksmith.message.receiver

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import spock.lang.Specification

class VelocityReceiverFactoryTest extends Specification {

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def players = [Mock(Player), Mock(Player)]
        def console = Mock(ConsoleCommandSource)

        and:
        def server = Mock(ProxyServer)
        server.allPlayers >> players
        server.consoleCommandSource >> console
        VelocityReceiverFactory.setup(server)

        and:
        def factory = new VelocityReceiverFactory()

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
        def sender = Mock(CommandSource)

        and:
        def factory = new VelocityReceiverFactory()

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
        (factory instanceof VelocityReceiverFactory)

        where:
        receiverType << [
                CommandSource,
                ConsoleCommandSource,
                Player
        ]
    }

}
