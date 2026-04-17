//file:noinspection unused
package it.fulminazzo.blocksmith.command.visitor.execution

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.ConsoleCommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.Player
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.message.argument.Time
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import it.fulminazzo.blocksmith.validation.annotation.Matches
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Method
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CommandExecutionVisitorTest extends Specification {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor()

    private static final CommandSenderWrapper commandSender = new MockCommandSenderWrapper(
            new CommandSender().addPermissions('blocksmith.bypass.cooldown.bypassed.greet')
    )

    private static Method first = CommandExecutionVisitorTest.getDeclaredMethod('execute', CommandSender, String, String)
    private static Method second = CommandExecutionVisitorTest.getDeclaredMethod('execute', String, String)

    private static volatile String printer

    void setup() {
        printer = null
    }

    void cleanup() {
        printer = null
    }

    void cleanupSpec() {
        executorService.close()
    }

    def 'test that execute works'() {
        given:
        def say = newLiteral('say', 'yell')

        def hello = newLiteral('hello')
        say.addChild(hello)

        def greeting = ArgumentNode.of('greeting', String, false)
        hello.addChild(greeting)

        def who = ArgumentNode.of('who', String, true)
        who.defaultValue = defaultArg
        who.executor = new ExecutionHandler(CommandExecutionVisitorTest, method)

        if (whatEnabled) {
            def what = newLiteral('what')
            greeting.addChild(what)
            what.addChild(who)
        } else greeting.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'say',
                'hello', 'Hello'
        )
        if (whatEnabled) visitor.input.addInput('what')
        if (whoArg != null) visitor.input.addInput(whoArg)

        expect:
        printer == null

        when:
        say.accept(visitor)

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
        def node = newLiteral('test')

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'test'
        )

        when:
        node.accept(visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.not-enough-arguments'
    }

    def 'test execute with cooldown'() {
        given:
        def greet = newLiteral('greet')
        greet.commandInfo = new CommandInfo(
                '',
                new PermissionInfo('blocksmith', 'greet', Permission.Grant.ALL)
        )
        def node = ArgumentNode.of('cooldown', String, false)
        greet.addChild(node)
        def who = ArgumentNode.of('who', String, false)
        who.executor = new ExecutionHandler(CommandExecutionVisitorTest, second)
                .setCooldown(Duration.ofSeconds(1L))
        node.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'greet',
                'Hello', 'Alex'
        )

        expect:
        printer == null

        when:
        greet.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'

        when:
        visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'greet',
                'Hello', 'Alex'
        )

        and:
        greet.accept(visitor)

        then:
        def e = thrown(CommandExecutionException)
        def arg = e.arguments[0]
        (arg instanceof Time)

        and:
        arg.placeholder == 'cooldown'
        arg.timeFormat == 'general.time-format'

        and:
        def time = arg.timeSupplier.get()
        time <= 1000L
        time >= 0
    }

    def 'test execute with cooldown bypass'() {
        given:
        def greet = newLiteral('greet')
        greet.commandInfo = new CommandInfo(
                '',
                new PermissionInfo('blocksmith', 'bypassed.greet', Permission.Grant.ALL)
        )
        def node = ArgumentNode.of('cooldown', String, false)
        greet.addChild(node)
        def who = ArgumentNode.of('who', String, false)
        who.executor = new ExecutionHandler(CommandExecutionVisitorTest, second)
                .setCooldown(Duration.ofSeconds(1L))
        node.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'greet',
                'Hello', 'Alex'
        )

        expect:
        printer == null

        when:
        greet.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'

        when:
        visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'greet',
                'Hello', 'Alex'
        )

        and:
        greet.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test execute asynchronously'() {
        given:
        def greet = newLiteral('greet')
        greet.commandInfo = new CommandInfo(
                '',
                new PermissionInfo('blocksmith', 'greet', Permission.Grant.ALL)
        )
        def node = ArgumentNode.of('greeting', String, false)
        greet.addChild(node)
        def who = ArgumentNode.of('who', String, false)
        who.executor = new ExecutionHandler(CommandExecutionVisitorTest, second)
                .setAsync(executorService, Duration.ofSeconds(1))
        node.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'greet',
                'Hello', 'Alex'
        )

        expect:
        printer == null

        when:
        greet.accept(visitor)

        and:
        sleep(200)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test execute with extra arguments'() {
        given:
        def commandNode = newLiteral('greet')
        def node = ArgumentNode.of('greeting', String, false)
        commandNode.addChild(node)
        def who = ArgumentNode.of('who', String, false)
        who.executor = new ExecutionHandler(CommandExecutionVisitorTest, second)
        node.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'Hello',
                'Alex', 'extra', 'input', 'should', 'be', 'ignored'
        )

        expect:
        printer == null

        when:
        node.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test that execute throws CommandExecuteException on invalid parameter'() {
        given:
        def say = newLiteral('say', 'yell')

        def greeting = ArgumentNode.of('greeting', String, false)
        say.addChild(greeting)

        def who = ArgumentNode.of('who', String, true)
        who.executor = new ExecutionHandler(
                this,
                CommandExecutionVisitorTest.getMethod('checkedExecute', String, String)
        )
        greeting.addChild(who)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'say',
                'hello', '!@#$%^&*()_+'
        )

        when:
        say.accept(visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.invalid-arguments'

        and:
        def messages = e.additionalMessages
        messages.size() == 1

        and:
        def message = 'error.invalid-name'
        def arguments = messages[message]
        arguments != null
        arguments.length == 2

        and:
        def first = arguments[0]
        first.placeholder == '%expected%'
        ComponentUtils.toString(first.value) == '^[A-Za-z]+$'

        and:
        def second = arguments[1]
        second.placeholder == '%value%'
        ComponentUtils.toString(second.value) == '!@#$%^&*()_+'
    }

    def 'test that execute throws on unknown command'() {
        given:
        def node = newLiteral('test')

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'test',
                'help'
        )

        when:
        node.accept(visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.command-not-found'
    }

    def 'test that execute throws CommandExecuteException on #exception'() {
        given:
        def name = exception.class.simpleName.uncapitalize()

        and:
        def node = newLiteral(name)
        node.executor = new ExecutionHandler(CommandExecutionVisitorTest, CommandExecutionVisitorTest.getDeclaredMethod(name))

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                name
        )

        when:
        node.accept(visitor)

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

    def 'test that execute supports greedy argument'() {
        given:
        def node = newLiteral('message')

        def message = ArgumentNode.of('message', String, false)
        message.greedy = true
        message.executor = new ExecutionHandler(
                CommandExecutionVisitorTest,
                CommandExecutionVisitorTest.getMethod('message', CommandSenderWrapper, String)
        )
        node.addChild(message)

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                node.name,
                'Hello,', 'Alex!'
        )

        expect:
        printer == null

        when:
        node.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == 'Hello, Alex!'
    }

    def 'test that execute of #method from #sender does not throw'() {
        given:
        def node = newLiteral('command')
        node.executor = new ExecutionHandler(
                CommandExecutionVisitorTest,
                method
        )

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                new MockCommandSenderWrapper(sender),
                'command'
        )

        expect:
        printer == null

        when:
        node.accept(visitor)

        then:
        noExceptionThrown()

        and:
        printer == "Hello from $sender.name!"

        where:
        method                                                                     | sender
        CommandExecutionVisitorTest.getMethod('consoleOnly', ConsoleCommandSender) | new ConsoleCommandSender()
        CommandExecutionVisitorTest.getMethod('playerOnly', Player)                | new Player('Alex')
    }

    def 'test that execute of #method from #sender throws CommandExecutionException with #message'() {
        given:
        def node = newLiteral('command')
        node.executor = new ExecutionHandler(
                CommandExecutionVisitorTest,
                method
        )

        and:
        def visitor = new CommandExecutionVisitor(
                Mock(ApplicationHandle),
                new MockCommandSenderWrapper(sender),
                'command'
        )

        when:
        node.accept(visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == message

        where:
        method                                                                     | sender                     || message
        CommandExecutionVisitorTest.getMethod('consoleOnly', ConsoleCommandSender) | new Player('Alex')         || 'error.player-cannot-execute'
        CommandExecutionVisitorTest.getMethod('playerOnly', Player)                | new ConsoleCommandSender() || 'error.console-cannot-execute'
    }

    private static final LiteralNode newLiteral(final String... aliases) {
        def node = new LiteralNode(aliases)
        node.commandInfo = new CommandInfo(
                "Description for literal node ${aliases[0]}",
                new PermissionInfo(
                        'blocksmith',
                        aliases[0],
                        Permission.Grant.ALL
                )
        )
        return node
    }

    /*
     * TEST METHODS
     */

    static void execute(final @NotNull CommandSender sender,
                        final @NotNull String greeting,
                        final @NotNull String who) {
        printer = "$greeting, $who!"
    }

    static void execute(final @NotNull String greeting,
                        final @NotNull String who) {
        printer = "$greeting, $who!"
    }

    void checkedExecute(final @NotNull String greeting,
                        final @NotNull @Matches(
                                value = '^[A-Za-z]+$',
                                message = 'error.invalid-name'
                        ) String who) {
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

}
