//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.ConsoleCommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.Player
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import it.fulminazzo.blocksmith.message.argument.Time
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Method
import java.time.Duration

class CommandNodeExecuteTest extends Specification {
    private static final @NotNull
    CommandSenderWrapper commandSender = new MockCommandSenderWrapper(new CommandSender())

    private static @NotNull
    Method first = CommandNodeExecuteTest.getDeclaredMethod('execute', CommandSender, String, String)
    private static @NotNull
    Method second = CommandNodeExecuteTest.getDeclaredMethod('execute', String, String)

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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
                .addInput('test')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.not-enough-arguments'
    }

    def 'test execute with cooldown'() {
        given:
        def node = new ArgumentNode('cooldown', String, false)
        def who = new ArgumentNode('who', String, false)
        who.executionInfo = new ExecutionInfo(CommandNodeExecuteTest, second)
        who.cooldown = Duration.ofSeconds(1L)
        node.addChild(who)

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
                .addInput('Hello', 'Alex')

        expect:
        printer == null

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'

        when:
        context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
                .addInput('Hello', 'Alex')

        and:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        def arg = e.arguments[0]
        (arg instanceof Time)

        and:
        arg.placeholder == 'cooldown'
        arg.timeFormat == 'general.time-format'

        and:
        def time = arg.timeSupplier.get()
        time < 1000L
        time > 0
    }

    def 'test execute with extra arguments'() {
        given:
        def node = new ArgumentNode('greeting', String, false)
        def who = new ArgumentNode('who', String, false)
        who.executionInfo = new ExecutionInfo(CommandNodeExecuteTest, second)
        node.addChild(who)

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
                .addInput(node.name)
        context.addParsedArgument(1)
        context.addParsedArgument(2)

        when:
        node.execute(context)

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that execute supports greedy argument'() {
        given:
        def node = new LiteralNode('message')

        def message = new ArgumentNode('message', String, false)
        message.greedy = true
        message.executionInfo = new ExecutionInfo(
                CommandNodeExecuteTest,
                CommandNodeExecuteTest.getMethod('message', CommandSenderWrapper, String)
        )
        node.addChild(message)

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), commandSender)
                .addInput(node.name, 'Hello,', 'Alex!')

        expect:
        printer == null

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test that execute of #method from #sender does not throw'() {
        given:
        def node = new LiteralNode('command')
        node.executionInfo = new ExecutionInfo(
                CommandNodeExecuteTest,
                method
        )

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), new MockCommandSenderWrapper(sender))
                .addInput('command')

        expect:
        printer == null

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        and:
        printer == "Hello from $sender.name!"

        where:
        method                                                                | sender
        CommandNodeExecuteTest.getMethod('consoleOnly', ConsoleCommandSender) | new ConsoleCommandSender()
        CommandNodeExecuteTest.getMethod('playerOnly', Player)                | new Player('Alex')
    }

    def 'test that execute of #method from #sender throws CommandExecutionException with #message'() {
        given:
        def node = new LiteralNode('command')
        node.executionInfo = new ExecutionInfo(
                CommandNodeExecuteTest,
                method
        )

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), new MockCommandSenderWrapper(sender))
                .addInput('command')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == message

        where:
        method                                                                | sender                     || message
        CommandNodeExecuteTest.getMethod('consoleOnly', ConsoleCommandSender) | new Player('Alex')         || 'error.player-cannot-execute'
        CommandNodeExecuteTest.getMethod('playerOnly', Player)                | new ConsoleCommandSender() || 'error.console-cannot-execute'
    }

    def 'test that tabComplete with #arguments returns #expected'() {
        given:
        def node = new LiteralNode('give')

        def item = new ArgumentNode('item', String, false)
        node.addChild(item)

        def player = new LiteralNode('player')
        node.addChild(player)

        def playerArg = new ArgumentNode('player', String, false)
        player.addChild(playerArg)

        item = new ArgumentNode('item', String, false)
        playerArg.addChild(item)

        and:
        def context = new CommandExecutionContext(
                Mock(ApplicationHandle),
                new MockCommandSenderWrapper(new CommandSender())
        ).addInput(*arguments)

        when:
        def actual = node.tabComplete(context)

        then:
        actual.sort() == expected.sort()

        where:
        arguments                                   || expected
        []                                          || []
        ['give', '']                                || ['player', '<item>']
        ['give', 'p']                               || ['player']
        ['give', 'P']                               || ['player']
        ['give', 'a']                               || ['<item>']
        ['give', 'player']                          || ['player']
        ['give', 'playera']                         || ['<item>']
        ['give', 'player', '']                      || ['<player>']
        ['give', 'player', 'Alex']                  || ['<player>']
        ['give', 'player', 'Alex', '']              || ['<item>']
        ['give', 'player', 'Alex', 'diamond_sword'] || ['<item>']
    }

    def 'test that tabComplete of greedy parameter with #arguments returns #expected'() {
        given:
        def node = new LiteralNode('msg')

        def player = new ArgumentNode('player', String, false)
        node.addChild(player)

        def message = new ArgumentNode('message', String, false)
        message.greedy = true
        player.addChild(message)

        and:
        def context = new CommandExecutionContext(
                Mock(ApplicationHandle),
                new MockCommandSenderWrapper(new CommandSender())
        ).addInput(*arguments)

        when:
        def actual = node.tabComplete(context)

        then:
        actual.sort() == expected.sort()

        where:
        arguments                                                  || expected
        ['msg', 'Alex', '']                                        || ['<message>']
        ['msg', 'Alex', 'Hello']                                   || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!']                        || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,']              || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!']     || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!', ''] || ['<message>']
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

    static void message(final @NotNull CommandSenderWrapper sender,
                        final @NotNull String message) {
        printer = message
    }

    static void consoleOnly(final @NotNull ConsoleCommandSender sender) {
        printer = "Hello from $sender.name!"
    }

    static void playerOnly(final @NotNull Player sender) {
        printer = "Hello from $sender.name!"
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
