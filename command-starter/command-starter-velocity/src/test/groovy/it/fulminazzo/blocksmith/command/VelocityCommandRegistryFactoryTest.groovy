package it.fulminazzo.blocksmith.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import spock.lang.Specification

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

class VelocityCommandRegistryFactoryTest extends Specification {

    private ApplicationHandle application

    private InputVisitor<?, ? extends Exception> visitor

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
        server.consoleCommandSource >> Mock(ConsoleCommandSource)

        server.allPlayers >> [player1, player2]
        server.getPlayer(_ as String) >> { a ->
            Optional.ofNullable(server.allPlayers.find { it.username.equalsIgnoreCase(a[0]) })
        }

        server.allServers >> [server1, server2]
        server.getServer(_ as String) >> { a ->
            Optional.ofNullable(server.allServers.find { it.serverInfo.name.equalsIgnoreCase(a[0]) })
        }

        application = Mock(ApplicationHandle)
        application.server() >> server
        application.commandRegistry >> {
            def registry = Mock(CommandRegistry)
            registry.wrapSender(_) >> { a -> new VelocityCommandSenderWrapper(application, a[0]) }
            return registry
        }

        def input = new CommandInput()
        visitor = Mock(InputVisitor)
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
        type                                                 | argument  || expected
        // PLAYER
        Player                                               | 'Alex'    || { a -> a.server().getPlayer('Alex').get() }
        Player                                               | 'Camilla' || { a -> a.server().getPlayer('Camilla').get() }
        // CONSOLE COMMAND SOURCE
        ConsoleCommandSource                                 | 'console' || { a -> a.server().consoleCommandSource }
        // COMMAND SOURCE
        CommandSource                                        | 'Alex'    || { a -> a.server().getPlayer('Alex').get() }
        CommandSource                                        | 'Camilla' || { a -> a.server().getPlayer('Camilla').get() }
        CommandSource                                        | 'console' || { a -> a.server().consoleCommandSource }
        // PLAYER WRAPPER
        commandSenderWrapper(Player)                         | 'Alex'    || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Alex').get()) }
        commandSenderWrapper(Player)                         | 'Camilla' || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Camilla').get()) }
        // CONSOLE COMMAND SOURCE WRAPPER
        commandSenderWrapper(ConsoleCommandSource)           | 'console' || { a -> new VelocityCommandSenderWrapper(a, a.server().consoleCommandSource) }
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                                 | 'Alex'    || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Alex').get()) }
        CommandSenderWrapper                                 | 'Camilla' || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Camilla').get()) }
        CommandSenderWrapper                                 | 'console' || { a -> new VelocityCommandSenderWrapper(a, a.server().consoleCommandSource) }
        // PLAYER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Player))               | 'Alex'    || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Alex').get()) }
        commandSenderWrapper(wildcard(Player))               | 'Camilla' || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Camilla').get()) }
        // CONSOLE COMMAND SOURCE WILDCARD WRAPPER
        commandSenderWrapper(wildcard(ConsoleCommandSource)) | 'console' || { a -> new VelocityCommandSenderWrapper(a, a.server().consoleCommandSource) }
        // COMMAND SENDER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Object))               | 'Alex'    || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Alex').get()) }
        commandSenderWrapper(wildcard(Object))               | 'Camilla' || { a -> new VelocityCommandSenderWrapper(a, a.server().getPlayer('Camilla').get()) }
        commandSenderWrapper(wildcard(Object))               | 'console' || { a -> new VelocityCommandSenderWrapper(a, a.server().consoleCommandSource) }
        // SERVER
        RegisteredServer                                     | 'Lobby'   || { a -> a.server().getServer('Lobby').get() }
        RegisteredServer                                     | 'Bedwars' || { a -> a.server().getServer('Bedwars').get() }
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
        type                                                 | argument   || expected
        // PLAYER
        Player                                               | ''         || 'error.player-not-found'
        Player                                               | 'A'        || 'error.player-not-found'
        Player                                               | 'C'        || 'error.player-not-found'
        Player                                               | 'c'        || 'error.player-not-found'
        Player                                               | 'steve'    || 'error.player-not-found'
        // CONSOLE COMMAND SOURCE
        ConsoleCommandSource                                 | 'z'        || 'error.unrecognized-argument'
        // COMMAND SOURCE
        CommandSource                                        | ''         || 'error.player-not-found'
        CommandSource                                        | 'A'        || 'error.player-not-found'
        CommandSource                                        | 'C'        || 'error.player-not-found'
        CommandSource                                        | 'c'        || 'error.player-not-found'
        CommandSource                                        | 'steve'    || 'error.player-not-found'
        CommandSource                                        | 'z'        || 'error.player-not-found'
        // PLAYER WRAPPER
        commandSenderWrapper(Player)                         | ''         || 'error.player-not-found'
        commandSenderWrapper(Player)                         | 'A'        || 'error.player-not-found'
        commandSenderWrapper(Player)                         | 'C'        || 'error.player-not-found'
        commandSenderWrapper(Player)                         | 'c'        || 'error.player-not-found'
        commandSenderWrapper(Player)                         | 'steve'    || 'error.player-not-found'
        // CONSOLE COMMAND SOURCE WRAPPER
        commandSenderWrapper(ConsoleCommandSource)           | 'z'        || 'error.unrecognized-argument'
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                                 | ''         || 'error.player-not-found'
        CommandSenderWrapper                                 | 'A'        || 'error.player-not-found'
        CommandSenderWrapper                                 | 'C'        || 'error.player-not-found'
        CommandSenderWrapper                                 | 'c'        || 'error.player-not-found'
        CommandSenderWrapper                                 | 'steve'    || 'error.player-not-found'
        CommandSenderWrapper                                 | 'z'        || 'error.player-not-found'
        // PLAYER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Player))               | ''         || 'error.player-not-found'
        commandSenderWrapper(wildcard(Player))               | 'A'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Player))               | 'C'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Player))               | 'c'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Player))               | 'steve'    || 'error.player-not-found'
        // CONSOLE COMMAND SOURCE WILDCARD WRAPPER
        commandSenderWrapper(wildcard(ConsoleCommandSource)) | 'z'        || 'error.unrecognized-argument'
        // COMMAND SENDER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Object))               | ''         || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))               | 'A'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))               | 'C'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))               | 'c'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))               | 'steve'    || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))               | 'z'        || 'error.player-not-found'
        // SERVER
        RegisteredServer                                     | ''         || 'error.server-not-found'
        RegisteredServer                                     | 'L'        || 'error.server-not-found'
        RegisteredServer                                     | 'B'        || 'error.server-not-found'
        RegisteredServer                                     | 'b'        || 'error.server-not-found'
        RegisteredServer                                     | 'survival' || 'error.server-not-found'
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
        type                                                 | argument   || expected
        // PLAYER
        Player                                               | ''         || ['Alex', 'Camilla']
        Player                                               | 'A'        || ['Alex', 'Camilla']
        Player                                               | 'Alex'     || ['Alex', 'Camilla']
        Player                                               | 'C'        || ['Alex', 'Camilla']
        Player                                               | 'Camilla'  || ['Alex', 'Camilla']
        Player                                               | 'c'        || ['Alex', 'Camilla']
        Player                                               | 'steve'    || ['Alex', 'Camilla']
        // CONSOLE
        ConsoleCommandSource                                 | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        ConsoleCommandSource                                 | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        ConsoleCommandSource                                 | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        // COMMAND SENDER
        CommandSource                                        | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSource                                        | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // PLAYER WRAPPER
        commandSenderWrapper(Player)                         | ''         || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'A'        || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'Alex'     || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'C'        || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'Camilla'  || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'c'        || ['Alex', 'Camilla']
        commandSenderWrapper(Player)                         | 'steve'    || ['Alex', 'Camilla']
        // CONSOLE WRAPPER
        commandSenderWrapper(ConsoleCommandSource)           | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        commandSenderWrapper(ConsoleCommandSource)           | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        commandSenderWrapper(ConsoleCommandSource)           | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                                 | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                                 | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // PLAYER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Player))               | ''         || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'A'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'Alex'     || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'C'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'Camilla'  || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'c'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(Player))               | 'steve'    || ['Alex', 'Camilla']
        // CONSOLE WILDCARD WRAPPER
        commandSenderWrapper(wildcard(ConsoleCommandSource)) | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        commandSenderWrapper(wildcard(ConsoleCommandSource)) | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        commandSenderWrapper(wildcard(ConsoleCommandSource)) | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME]
        // COMMAND SENDER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Object))               | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))               | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // SERVER
        RegisteredServer                                     | ''         || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'L'        || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'Lobby'    || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'B'        || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'Bedwars'  || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'b'        || ['Lobby', 'Bedwars']
        RegisteredServer                                     | 'survival' || ['Lobby', 'Bedwars']
    }

    private static Type commandSenderWrapper(final Type type) {
        return new ParameterizedType() {

            @Override
            Type[] getActualTypeArguments() {
                return [type].toArray(new Type[1])
            }

            @Override
            Type getRawType() {
                return CommandSenderWrapper
            }

            @Override
            Type getOwnerType() {
                return null
            }

        }
    }

    private static Type wildcard(final Type upperBound) {
        return new WildcardType() {

            @Override
            Type[] getUpperBounds() {
                return [upperBound].toArray(new Type[1])
            }

            @Override
            Type[] getLowerBounds() {
                return null
            }

        }
    }

}
