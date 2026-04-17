package it.fulminazzo.blocksmith.command.node_old.node
////file:noinspection unused
//TODO: update
//package it.fulminazzo.blocksmith.command.node
//
//import it.fulminazzo.blocksmith.ApplicationHandle
//import it.fulminazzo.blocksmith.command.*
//import it.fulminazzo.blocksmith.command.annotation.Permission
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
//import it.fulminazzo.blocksmith.message.argument.Time
//import it.fulminazzo.blocksmith.message.util.ComponentUtils
//import jakarta.validation.constraints.Pattern
//import org.jetbrains.annotations.NotNull
//import spock.lang.Specification
//
//import java.lang.reflect.Method
//import java.time.Duration
//
//class CommandNodeExecuteTest extends Specification {
//    private static final @NotNull CommandSenderWrapper commandSender = new MockCommandSenderWrapper(new CommandSender().addPermissions('blocksmith.bypass.cooldown.bypassed.greet'))
//
//    private static @NotNull Method first = CommandNodeExecuteTest.getDeclaredMethod('execute', CommandSender, String, String)
//    private static @NotNull Method second = CommandNodeExecuteTest.getDeclaredMethod('execute', String, String)
//
//    private static volatile String printer
//
//    void setup() {
//        printer = null
//    }
//
//    void cleanup() {
//        printer = null
//    }
//
//    def 'test that tabComplete with #arguments returns #expected'() {
//        given:
//        def node = new LiteralNode('give')
//
//        def item = new ArgumentNode('item', String, false)
//        node.addChild(item)
//
//        def player = new LiteralNode('player')
//        node.addChild(player)
//
//        def playerArg = new ArgumentNode('player', String, false)
//        player.addChild(playerArg)
//
//        item = new ArgumentNode('item', String, false)
//        playerArg.addChild(item)
//
//        and:
//        def context = new CommandExecutionContext(
//                Mock(ApplicationHandle),
//                Mock(CommandRegistry),
//                new MockCommandSenderWrapper(new CommandSender())
//        ).addInput(*arguments)
//
//        when:
//        def actual = node.tabComplete(context)
//
//        then:
//        actual.sort() == expected.sort()
//
//        where:
//        arguments                                   || expected
//        []                                          || []
//        ['give', '']                                || ['player', '<item>']
//        ['give', 'p']                               || ['player']
//        ['give', 'P']                               || ['player']
//        ['give', 'a']                               || ['<item>']
//        ['give', 'player']                          || ['player']
//        ['give', 'playera']                         || ['<item>']
//        ['give', 'player', '']                      || ['<player>']
//        ['give', 'player', 'Alex']                  || ['<player>']
//        ['give', 'player', 'Alex', '']              || ['<item>']
//        ['give', 'player', 'Alex', 'diamond_sword'] || ['<item>']
//    }
//
//    def 'test that tabComplete of greedy parameter with #arguments returns #expected'() {
//        given:
//        def node = new LiteralNode('msg')
//
//        def player = new ArgumentNode('player', String, false)
//        node.addChild(player)
//
//        def message = new ArgumentNode('message', String, false)
//        message.greedy = true
//        player.addChild(message)
//
//        and:
//        def context = new CommandExecutionContext(
//                Mock(ApplicationHandle),
//                Mock(CommandRegistry),
//                new MockCommandSenderWrapper(new CommandSender())
//        ).addInput(*arguments)
//
//        when:
//        def actual = node.tabComplete(context)
//
//        then:
//        actual.sort() == expected.sort()
//
//        where:
//        arguments                                                  || expected
//        ['msg', 'Alex', '']                                        || ['<message>']
//        ['msg', 'Alex', 'Hello']                                   || ['<message>']
//        ['msg', 'Alex', 'Hello,', 'world!']                        || ['<message>']
//        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,']              || ['<message>']
//        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!']     || ['<message>']
//        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!', ''] || ['<message>']
//    }
//
//    static void execute(final @NotNull CommandSender sender,
//                        final @NotNull String greeting,
//                        final @NotNull String who) {
//        printer = "$greeting, $who!"
//    }
//
//    static void execute(final @NotNull String greeting,
//                        final @NotNull String who) {
//        printer = "$greeting, $who!"
//    }
//
//    void checkedExecute(final @NotNull String greeting,
//                        final @NotNull @Pattern(
//                                regexp = '^[A-Za-z]+$',
//                                message = 'error.invalid-name'
//                        ) String who) {
//        printer = "$greeting, $who!"
//    }
//
//    static void message(final @NotNull CommandSenderWrapper sender,
//                        final @NotNull String message) {
//        printer = message
//    }
//
//    static void consoleOnly(final @NotNull ConsoleCommandSender sender) {
//        printer = "Hello from $sender.name!"
//    }
//
//    static void playerOnly(final @NotNull Player sender) {
//        printer = "Hello from $sender.name!"
//    }
//
//    static void runtimeException() {
//        throw new RuntimeException('Test runtime exception')
//    }
//
//    static void exception() {
//        throw new Exception('Test exception')
//    }
//
//    private static void privateMethod(int a, int b) {
//
//    }
//
//}
