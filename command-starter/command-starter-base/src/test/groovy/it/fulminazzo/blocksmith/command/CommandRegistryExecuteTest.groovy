//file:noinspection unused
package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.message.argument.Argument
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import spock.lang.Specification

class CommandRegistryExecuteTest extends Specification {
    private static boolean executed

    private Logger logger
    private Messenger messenger
    private CommandRegistry registry

    void setup() {
        executed = false

        logger = Mock(Logger)
        messenger = Mock(Messenger)
        registry = new MockCommandRegistry(messenger, logger)
    }

    void cleanup() {
        executed = false
    }

    def 'test that execute of valid method does not throw'() {
        given:
        def node = newLiteralNode('valid')
        node.executor = new ExecutionHandler(
                CommandRegistryExecuteTest,
                CommandRegistryExecuteTest.getMethod('valid')
        )

        when:
        registry.execute(node, executor, 'valid')

        then:
        noExceptionThrown()

        and:
        executed

        where:
        executor << [
                new CommandSender(),
                new ConsoleCommandSender(),
                new Player('Alex'),
                new MockCommandSenderWrapper(new CommandSender()),
                new MockCommandSenderWrapper(new ConsoleCommandSender()),
                new MockCommandSenderWrapper(new Player('Alex'))
        ]
    }

    def 'test that execute with not enough arguments does not throw'() {
        given:
        def node = newLiteralNode('valid')

        when:
        registry.execute(node, executor, 'valid')

        then:
        noExceptionThrown()

        and:
        !executed

        and:
        1 * messenger.sendMessage(
                executor instanceof CommandSenderWrapper ? executor : new MockCommandSenderWrapper(executor),
                'error.not-enough-arguments',
                _ as Argument[]
        )

        where:
        executor << [
                new CommandSender(),
                new ConsoleCommandSender(),
                new Player('Alex'),
                new MockCommandSenderWrapper(new CommandSender()),
                new MockCommandSenderWrapper(new ConsoleCommandSender()),
                new MockCommandSenderWrapper(new Player('Alex'))
        ]
    }

    def 'test that execute with CommandExecutionException does not throw'() {
        given:
        def node = newLiteralNode('valid')
        node.executor = new ExecutionHandler(
                CommandRegistryExecuteTest,
                CommandRegistryExecuteTest.getMethod('mockError')
        )

        and:
        def executor = new CommandSender()

        when:
        registry.execute(node, executor, 'valid')

        then:
        noExceptionThrown()

        and:
        !executed

        and:
        1 * messenger.sendMessage(new MockCommandSenderWrapper(executor), 'error.mock-error', _ as Argument[])
    }

    def 'test that internal exception is properly logged'() {
        given:
        def node = newLiteralNode('valid')
        node.executor = new ExecutionHandler(
                CommandRegistryExecuteTest,
                CommandRegistryExecuteTest.getMethod('invalid')
        )

        when:
        registry.execute(node, executor, 'valid', 'first', 'second', 'third')

        then:
        noExceptionThrown()

        and:
        !executed

        and:
        1 * logger.warn('{} while executing command /{}',
                RuntimeException.canonicalName,
                'valid first second third',
                _ as RuntimeException
        )

        where:
        executor << [
                new CommandSender(),
                new ConsoleCommandSender(),
                new Player('Alex'),
                new MockCommandSenderWrapper(new CommandSender()),
                new MockCommandSenderWrapper(new ConsoleCommandSender()),
                new MockCommandSenderWrapper(new Player('Alex'))
        ]
    }

    def 'test that tabComplete of valid method does not throw'() {
        given:
        def node = newLiteralNode('valid')
        node.addChild(newLiteralNode('first'))
        node.addChild(newLiteralNode('second'))
        node.addChild(newLiteralNode('third'))

        when:
        def actual = registry.tabComplete(node, executor, 'valid', '')

        then:
        actual.sort() == ['first', 'second', 'third'].sort()

        where:
        executor << [
                new CommandSender(),
                new ConsoleCommandSender(),
                new Player('Alex'),
                new MockCommandSenderWrapper(new CommandSender()),
                new MockCommandSenderWrapper(new ConsoleCommandSender()),
                new MockCommandSenderWrapper(new Player('Alex'))
        ]
    }

    private static final LiteralNode newLiteralNode(final @NotNull String name) {
        def node = new LiteralNode(name)
        node.commandInfo = new CommandInfo(
                'valid description',
                new PermissionInfo(null, 'valid permission', Permission.Grant.ALL)
        )
        return node
    }

    /*
     * TEST METHODS
     */

    static void valid() {
        executed = true
    }

    static void arg1(final String argument) {
        executed = true
    }

    static void invalid() {
        throw new RuntimeException('Test runtime exception')
    }

    static void mockError() {
        throw new CommandExecutionException('error.mock-error')
    }

}
