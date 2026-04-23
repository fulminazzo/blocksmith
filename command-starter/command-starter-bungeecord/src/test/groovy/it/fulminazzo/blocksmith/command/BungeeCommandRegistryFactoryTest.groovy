package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import spock.lang.Specification

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

@Slf4j
class BungeeCommandRegistryFactoryTest extends Specification {

    private ApplicationHandle application

    private InputVisitor<?, ? extends Exception> visitor

    void setup() {
        final player1 = Mock(ProxiedPlayer)
        player1.name >> 'Alex'
        final player2 = Mock(ProxiedPlayer)
        player2.name >> 'Camilla'

        final server1 = Mock(ServerInfo)
        server1.name >> 'Lobby'
        final server2 = Mock(ServerInfo)
        server2.name >> 'Bedwars'

        final server = Mock(ProxyServer)
        server.console >> Mock(CommandSender)

        server.players >> [player1, player2]
        server.getPlayer(_ as String) >> { a ->
            server.players.find { it.name.equalsIgnoreCase(a[0]) }
        }

        server.servers >> [(server1.name): server1, (server2.name): server2]
        server.getServerInfo(_ as String) >> { a ->
            server.servers
                    .collect { it.value }
                    .find { it.name.equalsIgnoreCase(a[0]) }
        }

        application = Mock(Plugin, additionalInterfaces: [ApplicationHandle]) as ApplicationHandle
        application.proxy >> server
        application.server() >> {
            return application.proxy
        }
        application.commandRegistry >> {
            def registry = Mock(CommandRegistry)
            registry.wrapSender(_) >> { a -> new BungeeCommandSenderWrapper(application, a[0]) }
            return registry
        }

        def input = new CommandInput()
        visitor = Mock(InputVisitor)
        visitor.application >> application
        visitor.input >> input
        visitor.commandSender >> new BungeeCommandSenderWrapper(application, Mock(CommandSender))
    }

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        (registry instanceof BungeeCommandRegistry)
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
        type                                          | argument  || expected
        // PLAYER
        ProxiedPlayer                                 | 'Alex'    || { a -> a.server().getPlayer('Alex') }
        ProxiedPlayer                                 | 'Camilla' || { a -> a.server().getPlayer('Camilla') }
        // COMMAND SENDER
        CommandSender                                 | 'Alex'    || { a -> a.server().getPlayer('Alex') }
        CommandSender                                 | 'Camilla' || { a -> a.server().getPlayer('Camilla') }
        CommandSender                                 | 'console' || { a -> a.server().console }
        // PLAYER WRAPPER
        commandSenderWrapper(ProxiedPlayer)           | 'Alex'    || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Alex')) }
        commandSenderWrapper(ProxiedPlayer)           | 'Camilla' || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Camilla')) }
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                          | 'Alex'    || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Alex')) }
        CommandSenderWrapper                          | 'Camilla' || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Camilla')) }
        CommandSenderWrapper                          | 'console' || { a -> new BungeeCommandSenderWrapper(a, a.server().console) }
        // PLAYER WRAPPER
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'Alex'    || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Alex')) }
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'Camilla' || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Camilla')) }
        // COMMAND SENDER WRAPPER
        commandSenderWrapper(wildcard(Object))        | 'Alex'    || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Alex')) }
        commandSenderWrapper(wildcard(Object))        | 'Camilla' || { a -> new BungeeCommandSenderWrapper(a, a.server().getPlayer('Camilla')) }
        commandSenderWrapper(wildcard(Object))        | 'console' || { a -> new BungeeCommandSenderWrapper(a, a.server().console) }
        // SERVER
        ServerInfo                                    | 'Lobby'   || { a -> a.server().getServerInfo('Lobby') }
        ServerInfo                                    | 'Bedwars' || { a -> a.server().getServerInfo('Bedwars') }
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
        type                                          | argument   || expected
        // PLAYER
        ProxiedPlayer                                 | ''         || 'error.player-not-found'
        ProxiedPlayer                                 | 'A'        || 'error.player-not-found'
        ProxiedPlayer                                 | 'C'        || 'error.player-not-found'
        ProxiedPlayer                                 | 'c'        || 'error.player-not-found'
        ProxiedPlayer                                 | 'steve'    || 'error.player-not-found'
        // COMMAND SENDER
        CommandSender                                 | 'z'        || 'error.player-not-found'
        CommandSender                                 | 'steve'    || 'error.player-not-found'
        CommandSender                                 | 'k'        || 'error.player-not-found'
        // PLAYER WRAPPER
        commandSenderWrapper(ProxiedPlayer)           | ''         || 'error.player-not-found'
        commandSenderWrapper(ProxiedPlayer)           | 'A'        || 'error.player-not-found'
        commandSenderWrapper(ProxiedPlayer)           | 'C'        || 'error.player-not-found'
        commandSenderWrapper(ProxiedPlayer)           | 'c'        || 'error.player-not-found'
        commandSenderWrapper(ProxiedPlayer)           | 'steve'    || 'error.player-not-found'
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                          | 'z'        || 'error.player-not-found'
        CommandSenderWrapper                          | 'steve'    || 'error.player-not-found'
        CommandSenderWrapper                          | 'k'        || 'error.player-not-found'
        // PLAYER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(ProxiedPlayer)) | ''         || 'error.player-not-found'
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'A'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'C'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'c'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'steve'    || 'error.player-not-found'
        // COMMAND SENDER WILDCARD WRAPPER
        commandSenderWrapper(wildcard(Object))        | 'z'        || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))        | 'steve'    || 'error.player-not-found'
        commandSenderWrapper(wildcard(Object))        | 'k'        || 'error.player-not-found'
        // SERVER
        ServerInfo                                    | ''         || 'error.server-not-found'
        ServerInfo                                    | 'L'        || 'error.server-not-found'
        ServerInfo                                    | 'B'        || 'error.server-not-found'
        ServerInfo                                    | 'b'        || 'error.server-not-found'
        ServerInfo                                    | 'survival' || 'error.server-not-found'
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
        type                                          | argument   || expected
        // PLAYER
        ProxiedPlayer                                 | ''         || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'A'        || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'Alex'     || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'C'        || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'Camilla'  || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'c'        || ['Alex', 'Camilla']
        ProxiedPlayer                                 | 'steve'    || ['Alex', 'Camilla']
        // COMMAND SENDER
        CommandSender                                 | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender                                 | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // PLAYER WRAPPER
        commandSenderWrapper(ProxiedPlayer)           | ''         || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'A'        || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'Alex'     || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'C'        || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'Camilla'  || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'c'        || ['Alex', 'Camilla']
        commandSenderWrapper(ProxiedPlayer)           | 'steve'    || ['Alex', 'Camilla']
        // COMMAND SENDER WRAPPER
        commandSenderWrapper(CommandSender)           | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(CommandSender)           | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // COMMAND SENDER WRAPPER
        CommandSenderWrapper                          | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSenderWrapper                          | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // PLAYER WRAPPER
        commandSenderWrapper(wildcard(ProxiedPlayer)) | ''         || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'A'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'Alex'     || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'C'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'Camilla'  || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'c'        || ['Alex', 'Camilla']
        commandSenderWrapper(wildcard(ProxiedPlayer)) | 'steve'    || ['Alex', 'Camilla']
        // COMMAND SENDER WRAPPER
        commandSenderWrapper(wildcard(CommandSender)) | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(CommandSender)) | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // COMMAND SENDER WRAPPER
        commandSenderWrapper(wildcard(Object))        | ''         || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'A'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'Alex'     || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'C'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'Camilla'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'c'        || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'steve'    || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        commandSenderWrapper(wildcard(Object))        | 'console'  || [CommandSenderWrapper.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // SERVER
        ServerInfo                                    | ''         || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'L'        || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'Lobby'    || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'B'        || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'Bedwars'  || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'b'        || ['Lobby', 'Bedwars']
        ServerInfo                                    | 'survival' || ['Lobby', 'Bedwars']
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
