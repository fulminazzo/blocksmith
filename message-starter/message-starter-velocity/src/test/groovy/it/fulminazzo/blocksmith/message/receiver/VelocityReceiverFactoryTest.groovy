package it.fulminazzo.blocksmith.message.receiver

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import it.fulminazzo.blocksmith.ServerApplication
import spock.lang.Specification

class VelocityReceiverFactoryTest extends Specification {
    private ProxyServer server
    private ServerApplication application

    void setup() {
        server = Mock(ProxyServer)

        application = Mock(ServerApplication)
        application.server() >> server
        application.as(_) >> application
    }

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def players = [Mock(Player), Mock(Player)]
        def console = Mock(ConsoleCommandSource)

        and:
        server.allPlayers >> players
        server.consoleCommandSource >> console

        and:
        def factory = new VelocityReceiverFactory().setup(application)

        when:
        def receivers = factory.allReceivers

        then:
        receivers != null

        when:
        def internalReceivers = receivers.collect { it.handle() }

        then:
        internalReceivers.sort() == [*players, console].sort()
    }

    def 'test that create does not throw'() {
        given:
        def sender = Mock(CommandSource)

        and:
        def factory = new VelocityReceiverFactory().setup(application)

        when:
        def receiver = factory.create(sender)

        then:
        receiver != null

        and:
        receiver.handle() == sender
    }

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType, application)

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
