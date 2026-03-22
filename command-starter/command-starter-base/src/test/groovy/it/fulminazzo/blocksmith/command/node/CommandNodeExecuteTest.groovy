//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Method

class CommandNodeExecuteTest extends Specification {
    private static @NotNull Method first = CommandNodeExecuteTest.getDeclaredMethod('execute', CommandSender, String, String)
    private static @NotNull Method second = CommandNodeExecuteTest.getDeclaredMethod('execute', String, String)

    private static String printer

    void setup() {
        printer = null
    }

    void cleanup() {
        printer = null
    }

    def 'test that execute works'() {
        given:
        def say = new LiteralNode('say', 'yell')

        def hello = new LiteralNode('hello')
        say.addChild(hello)

        def greeting = new ArgumentNode('greeting', String, false)
        hello.addChild(greeting)

        def who = new ArgumentNode('who', String, true)
        who.defaultValue = defaultArg
        who.executionInfo = new ExecutionInfo(CommandNodeExecuteTest, method)

        if (whatEnabled) {
            def what = new LiteralNode('what')
            greeting.addChild(what)
            what.addChild(who)
        } else greeting.addChild(who)

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput('say', 'hello', 'Hello')
        if (whatEnabled) context.addInput('what')
        if (whoArg != null) context.addInput(whoArg)

        expect:
        printer == null

        when:
        say.execute(context)

        then:
        noExceptionThrown()

        and:
        printer == "Hello, ${whoArg == null ? defaultArg : whoArg}!"

        where:
        method | whatEnabled | defaultArg || whoArg
        first  | false       | null       || 'Steve'
        first  | false       | null       || null
        first  | true        | null       || 'Steve'
        first  | true        | null       || null
        second | false       | null       || 'Steve'
        second | false       | null       || null
        second | true        | null       || 'Steve'
        second | true        | null       || null
        first  | false       | 'Alex'     || 'Steve'
        first  | false       | 'Alex'     || null
        first  | true        | 'Alex'     || 'Steve'
        first  | true        | 'Alex'     || null
        second | false       | 'Alex'     || 'Steve'
        second | false       | 'Alex'     || null
        second | true        | 'Alex'     || 'Steve'
        second | true        | 'Alex'     || null
    }

    def 'test that execute throws on non-executable node'() {
        given:
        def node = new LiteralNode('test')

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput('test')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.not-enough-arguments'
    }

    def 'test execute with extra arguments'() {
        given:
        def node = new ArgumentNode('greeting', String, false)
        def who = new ArgumentNode('who', String, false)
        who.executionInfo = new ExecutionInfo(CommandNodeExecuteTest, second)
        node.addChild(who)

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput('Hello', 'Alex', 'extra', 'input', 'should', 'be', 'ignored')

        expect:
        printer == null

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test that execute throws on unknown command'() {
        given:
        def node = new LiteralNode('test')

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput('test', 'help')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.command-not-found'
    }

    def 'test that execute throws CommandExecuteException on #exception'() {
        given:
        def name = exception.class.simpleName.uncapitalize()

        and:
        def node = new LiteralNode(name)
        node.executionInfo = new ExecutionInfo(CommandNodeExecuteTest, CommandNodeExecuteTest.getDeclaredMethod(name))

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput(name)

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.internal-error'

        and:
        def cause = e.cause
        cause.class == exception.class
        cause.message == exception.message

        where:
        exception << [
                new Exception('Test exception'),
                new RuntimeException('Test runtime exception')
        ]
    }

    def 'test that execute throws IllegalArgumentException on IllegalAccessException'() {
        given:
        def node = new LiteralNode('exception')
        node.executionInfo = new ExecutionInfo(
                CommandNodeExecuteTest,
                CommandNodeExecuteTest.getDeclaredMethod('privateMethod', int, int)
        )

        and:
        def context = new CommandExecutionContext(new CommandSender())
                .addInput(node.name)

        when:
        node.execute(context)

        then:
        thrown(IllegalArgumentException)
    }

    static void execute(final @NotNull CommandSender sender,
                        final @NotNull String greeting,
                        final @NotNull String who) {
        printer = "$greeting, $who!"
    }

    static void execute(final @NotNull String greeting,
                        final @NotNull String who) {
        printer = "$greeting, $who!"
    }

    static void runtimeException() {
        throw new RuntimeException('Test runtime exception')
    }

    static void exception() {
        throw new Exception('Test exception')
    }

    private static void privateMethod(int a, int b) {

    }

}
