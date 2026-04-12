//TODO: update
//package it.fulminazzo.blocksmith.command.argument
//
//import it.fulminazzo.blocksmith.ApplicationHandle
//import it.fulminazzo.blocksmith.command.CommandRegistry
//import it.fulminazzo.blocksmith.command.CommandSender
//import it.fulminazzo.blocksmith.command.CommandSenderWrapper
//import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
//import it.fulminazzo.blocksmith.message.argument.Placeholder
//import org.jetbrains.annotations.NotNull
//import spock.lang.Specification
//
//class NumberArgumentParserTest extends Specification {
//
//    private final NumberArgumentParser parser = new NumberArgumentParser<>(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::valueOf)
//
//    private final CommandSenderWrapper sender = new MockCommandSenderWrapper(new CommandSender())
//
//    def 'test that parse with valid argument does not throw'() {
//        when:
//        parser.parse(prepareContext('1'))
//
//        then:
//        noExceptionThrown()
//    }
//
//    def 'test that parse throws for invalid argument'() {
//        when:
//        parser.parse(prepareContext('a'))
//
//        then:
//        def e = thrown(CommandExecutionException)
//        e.arguments.toList() == [
//                Placeholder.of('argument', 'a'),
//                Placeholder.of("min", Integer.MIN_VALUE),
//                Placeholder.of("max", Integer.MAX_VALUE)
//        ]
//    }
//
//    def 'test that getCompletions returns #expected for argument #argument'() {
//        given:
//        def context = new CommandExecutionContext(
//                Mock(ApplicationHandle),
//                Mock(CommandRegistry),
//                sender
//        ).addInput(argument)
//
//        when:
//        def actual = parser.getCompletions(context)
//
//        then:
//        actual == expected
//
//        where:
//        argument             || expected
//        ''                   || (0..9).collect { it.toString() }
//        '1'                  || (0..9).collect { "1$it".toString() }
//        '12'                 || (0..9).collect { "12$it".toString() }
//        '-1'                 || (0..9).collect { "-1$it".toString() }
//        '-12'                || (0..9).collect { "-12$it".toString() }
//        'a'                  || []
//        "$Integer.MAX_VALUE" || []
//        "$Integer.MIN_VALUE" || []
//    }
//
//    private CommandExecutionContext prepareContext(final @NotNull String argument) {
//        def context = Mock(CommandExecutionContext)
//        context.current >> argument
//        return context
//    }
//
//}
