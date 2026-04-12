package it.fulminazzo.blocksmith.command.node.handler

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.ConsoleCommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.Player
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.visitor.execution.ExecutionContext
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class CommandExecutorTest extends Specification {

    private ExecutionContext context

    private Commands commands

    void setup() {
        def arguments = new LinkedList(['Hello, world!'])
        context = Mock(ExecutionContext)
        context.arguments >> arguments

        commands = Spy(Commands)
    }

    def 'test that execute with #sender and #method works'() {
        given:
        context.commandSender >> new MockCommandSenderWrapper(sender)

        and:
        def executor = new CommandExecutor(
                commands,
                Reflect.on(Commands).getMethod(m -> m.name == method)
        )

        when:
        executor.execute(context)

        then:
        1 * commands."$method"(*_) >> { a ->
            assert a == context.arguments
        }

        where:
        sender                     | method
        new Player('Alex')         | 'sendPlayerOnlyWrapper'
        new ConsoleCommandSender() | 'sendConsoleOnlyWrapper'
        new Player('Alex')         | 'sendPlayerOnly'
        new ConsoleCommandSender() | 'sendConsoleOnly'
        new CommandSender()        | 'send'
        new CommandSender()        | 'broadcast'
    }

    def 'test that execute with #sender and #method throws CommandExecutionException with message #expected'() {
        given:
        context.commandSender >> new MockCommandSenderWrapper(sender)

        and:
        def executor = new CommandExecutor(
                commands,
                Reflect.on(Commands).getMethod(m -> m.name == method)
        )

        when:
        executor.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == expected

        where:
        sender                     | method                   || expected
        new ConsoleCommandSender() | 'sendPlayerOnlyWrapper'  || 'error.console-cannot-execute'
        new Player('Alex')         | 'sendConsoleOnlyWrapper' || 'error.player-cannot-execute'
        new ConsoleCommandSender() | 'sendPlayerOnly'         || 'error.console-cannot-execute'
        new Player('Alex')         | 'sendConsoleOnly'        || 'error.player-cannot-execute'
    }

    def 'test that CommandExecutionException during execution is rethrown'() {
        given:
        def executor = new CommandExecutor(
                commands,
                Commands.getMethod('executionException', String)
        )

        when:
        executor.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'Execution exception!'
    }

    def 'test that general #exception during execution is thrown as CommandExecutionException'() {
        given:
        def executor = new CommandExecutor(
                commands,
                Commands.getMethod(exception, String)
        )

        when:
        executor.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.internal-error'

        and:
        def arguments = e.arguments
        arguments.length == 1

        and:
        def first = arguments[0]
        first.placeholder == '%message%'
        ComponentUtils.toString(first.value) == 'Commands have not been initialized!'

        where:
        exception << ['exception', 'runtimeException']
    }

}
