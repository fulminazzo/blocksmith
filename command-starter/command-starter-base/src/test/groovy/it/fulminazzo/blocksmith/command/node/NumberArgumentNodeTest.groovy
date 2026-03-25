package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import it.fulminazzo.blocksmith.message.argument.Placeholder
import spock.lang.Specification

class NumberArgumentNodeTest extends Specification {

    private NumberArgumentNode node
    private CommandExecutionContext context

    void setup() {
        node = new NumberArgumentNode('test', Integer, false)
                .min(1)
                .max(10)
        context = new CommandExecutionContext(
                Mock(ApplicationHandle),
                new MockCommandSenderWrapper(new CommandSender())
        )
    }

    def 'test that getCompletions return correct values'() {
        given:
        context.addInput('')

        when:
        def completions = node.getCompletions(context)

        then:
        completions == (1..9).collect { it.toString() }
    }

    def 'test that validateExecuteInput correctly validates #argument'() {
        given:
        context.addInput(argument)

        when:
        node.validateExecuteInput(context)

        then:
        noExceptionThrown()

        where:
        argument << (1..10).collect { it.toString() }
    }

    def 'test that validateExecuteInput throws for argument #argument'() {
        given:
        context.addInput(argument)

        when:
        node.validateExecuteInput(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.invalid-number'
        e.arguments.toList() == [
                Placeholder.of('argument', argument),
                Placeholder.of('min', 1),
                Placeholder.of('max', 10)
        ]

        where:
        argument << ['0', '11']
    }

    def 'test that validateTabCompleteInput correctly validates #argument'() {
        given:
        context.addInput(argument)

        when:
        node.validateTabCompleteInput(context)

        then:
        noExceptionThrown()

        where:
        argument << (1..10).collect { it.toString() }
    }

    def 'test that validateTabCompleteInput throws for argument #argument'() {
        given:
        context.addInput(argument)

        when:
        node.validateTabCompleteInput(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.invalid-number'
        e.arguments.toList() == [
                Placeholder.of('argument', argument),
                Placeholder.of('min', 1),
                Placeholder.of('max', 10)
        ]

        where:
        argument << ['0', '11']
    }

    def 'test that cast of type #type correctly casts to #expected'() {
        given:
        def node = new NumberArgumentNode('test', type, false)

        when:
        def actual = node.cast(10.0)

        then:
        actual == expected

        where:
        type    || expected
        Byte    || 10.byteValue()
        Short   || 10.shortValue()
        Integer || 10.intValue()
        Long    || 10.longValue()
        Float   || 10.floatValue()
        Double  || 10.floatValue()
    }

}
