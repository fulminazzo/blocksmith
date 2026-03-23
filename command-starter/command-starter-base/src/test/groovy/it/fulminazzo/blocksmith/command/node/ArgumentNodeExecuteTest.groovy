package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.argument.ArgumentParser
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class ArgumentNodeExecuteTest extends Specification {
    private static final List<String> numbers = (0..9).collect { it.toString() }

    private ArgumentParser<Integer> previous

    void setup() {
        previous = ArgumentParsers.parsers.get(Integer)
        ArgumentParsers.register(Integer, new ArgumentParser<Integer>() {

            @Override
            Integer parse(final @NotNull String rawArgument) throws CommandExecutionException {
                try {
                    return Integer.parseInt(rawArgument)
                } catch (NumberFormatException ex) {
                    throw new CommandExecutionException('error.mock-message', ex)
                }
            }

            @Override
            @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext c) {
                def current = c.current
                def completions = numbers
                        .collect { "$current$it" }
                        .findAll { it.isInteger() }
                        .collect { it.toString() }
                if (current.startsWith('b')) completions.add('<%name%>')
                return completions
            }

        })
    }

    void cleanup() {
        ArgumentParsers.parsers.put(Integer, previous)
    }

    def 'test that validateInput throws for invalid input'() {
        given:
        def node = new ArgumentNode('test', Integer, false)
        node.executionInfo = new ExecutionInfo(
                ArgumentNodeExecuteTest,
                ArgumentNodeExecuteTest.getMethod('mock')
        )

        and:
        def context = new CommandExecutionContext(new MockCommandSenderWrapper(new CommandSender()))
        context.addInput('invalid')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.mock-message'

        and:
        (e.cause instanceof NumberFormatException)
    }

    def 'test that getCompletions returns #expected for #argument'() {
        given:
        def node = new ArgumentNode('test', Integer, false)

        and:
        def context = new CommandExecutionContext(new MockCommandSenderWrapper(new CommandSender()))
        context.addInput(argument)

        when:
        def actual = node.getCompletions(context)

        then:
        actual == expected

        where:
        argument || expected
        ''       || numbers
        '1'      || numbers.collect { "1$it" }
        'a'      || []
        '1.'     || []
        '1.3'    || []
        'b'      || ['<test>']
    }

    @SuppressWarnings('unused')
    static void mock() {

    }

}
