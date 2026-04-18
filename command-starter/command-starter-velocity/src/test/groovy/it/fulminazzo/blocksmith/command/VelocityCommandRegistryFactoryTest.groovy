package it.fulminazzo.blocksmith.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

class VelocityCommandRegistryFactoryTest extends Specification {

    private ApplicationHandle application

    private Visitor<?, ?> visitor

    void setup() {
        final player1 = Mock(Player)
        player1.username >> 'Alex'
        final player2 = Mock(Player)
        player2.username >> 'Camilla'

        final server1 = Mock(RegisteredServer)
        server1.serverInfo >> {
            final serverInfo = Mock(ServerInfo)
            serverInfo.name >> 'Lobby'
            return serverInfo
        }
        final server2 = Mock(RegisteredServer)
        server2.serverInfo >> {
            final serverInfo = Mock(ServerInfo)
            serverInfo.name >> 'Bedwars'
            return serverInfo
        }

        final server = Mock(ProxyServer)

        server.allPlayers >> [player1, player2]
        server.getPlayer(_ as String) >> { a ->
            Optional.ofNullable(server.allPlayers.find { it.username.equalsIgnoreCase(a[0]) })
        }

        server.allServers >> [server1, server2]
        server.getServer(_ as String) >> { a ->
            Optional.ofNullable(server.allServers.find { it.serverInfo.name.equalsIgnoreCase(a[0]) })
        }

        application = Mock(ApplicationHandle)
        application.server >> server

        def input = new CommandInput()
        visitor = Mock(Visitor)
        visitor.application >> application
        visitor.input >> input
        visitor.commandSender >> new VelocityCommandSenderWrapper(application, Mock(CommandSource))
    }

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        (registry instanceof VelocityCommandRegistry)
    }

    def 'test that parse of parser for #type returns #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        visitor.input.addInput(argument)

        when:
        def actual = parser.parse(visitor)

        then:
        actual == expected(application)

        where:
        type             | argument  || expected
        // PLAYER
        Player           | 'Alex'    || { a -> a.server.getPlayer('Alex').get() }
        Player           | 'Camilla' || { a -> a.server.getPlayer('Camilla').get() }
        // SERVER
        RegisteredServer | 'Lobby'   || { a -> a.server.getServer('Lobby').get() }
        RegisteredServer | 'Bedwars' || { a -> a.server.getServer('Bedwars').get() }
    }

    def 'test that parse of parser for #type throws exception with #expected message with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        visitor.input.addInput(argument)

        when:
        parser.parse(visitor)

        then:
        def e = thrown(ArgumentParseException)
        e.message == expected

        where:
        type             | argument   || expected
        // PLAYER
        Player           | ''         || 'error.player-not-found'
        Player           | 'A'        || 'error.player-not-found'
        Player           | 'C'        || 'error.player-not-found'
        Player           | 'c'        || 'error.player-not-found'
        Player           | 'steve'    || 'error.player-not-found'
        // SERVER
        RegisteredServer | ''         || 'error.server-not-found'
        RegisteredServer | 'L'        || 'error.server-not-found'
        RegisteredServer | 'B'        || 'error.server-not-found'
        RegisteredServer | 'b'        || 'error.server-not-found'
        RegisteredServer | 'survival' || 'error.server-not-found'
    }

    def 'test that completions of parser for #type return #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        visitor.input.addInput(argument)

        when:
        def actual = parser.getCompletions(visitor)

        then:
        actual == expected

        where:
        type             | argument   || expected
        // PLAYER
        Player           | ''         || ['Alex', 'Camilla']
        Player           | 'A'        || ['Alex', 'Camilla']
        Player           | 'Alex'     || ['Alex', 'Camilla']
        Player           | 'C'        || ['Alex', 'Camilla']
        Player           | 'Camilla'  || ['Alex', 'Camilla']
        Player           | 'c'        || ['Alex', 'Camilla']
        Player           | 'steve'    || ['Alex', 'Camilla']
        // SERVER
        RegisteredServer | ''         || ['Lobby', 'Bedwars']
        RegisteredServer | 'L'        || ['Lobby', 'Bedwars']
        RegisteredServer | 'Lobby'    || ['Lobby', 'Bedwars']
        RegisteredServer | 'B'        || ['Lobby', 'Bedwars']
        RegisteredServer | 'Bedwars'  || ['Lobby', 'Bedwars']
        RegisteredServer | 'b'        || ['Lobby', 'Bedwars']
        RegisteredServer | 'survival' || ['Lobby', 'Bedwars']
    }

}
