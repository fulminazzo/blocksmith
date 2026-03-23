//file:noinspection unused
package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.ExecutionInfo
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.message.Messenger
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
        def node = new LiteralNode('valid')
        node.executionInfo = new ExecutionInfo(
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
        def node = new LiteralNode('valid')
        def argument = new ArgumentNode('arg', String, false)
        argument.executionInfo = new ExecutionInfo(
                CommandRegistryExecuteTest,
                CommandRegistryExecuteTest.getMethod('arg1', String)
        )

        when:
        registry.execute(node, executor, 'valid')

        then:
        noExceptionThrown()

        and:
        !executed

        and:
        1 * messenger.sendMessage(executor, 'error.not-enough-arguments')

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

    def 'test that internal exception is properly logged'() {
        given:
        def node = new LiteralNode('valid')
        node.executionInfo = new ExecutionInfo(
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
        1 * logger.warn('{} while executing command /{} {}',
                RuntimeException.canonicalName,
                'valid',
                'first second third',
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
        def node = new LiteralNode('valid')
        node.addChild(new LiteralNode('first'))
        node.addChild(new LiteralNode('second'))
        node.addChild(new LiteralNode('third'))

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

    static void valid() {
        executed = true
    }

    static void arg1(final String argument) {
        executed = true
    }

    static void invalid() {
        throw new RuntimeException('Test runtime exception')
    }

}
