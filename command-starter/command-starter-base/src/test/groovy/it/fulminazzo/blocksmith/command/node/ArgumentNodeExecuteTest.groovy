package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.argument.ArgumentParser
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class ArgumentNodeExecuteTest extends Specification {

    def 'test that validateInput throws for invalid input'() {
        given:
        ArgumentParsers.register(Integer, new ArgumentParser<Integer>() {

            @Override
            Integer parse(final @NotNull String rawArgument) throws CommandExecutionException {
                try {
                    return Integer.parseInt(rawArgument)
                } catch (NumberFormatException ex) {
                    throw new CommandExecutionException('error.mock-message', ex)
                }
            }

        })

        and:
        def node = new ArgumentNode('test', Integer, false)
        node.executionInfo = new ExecutionInfo(
                ArgumentNodeExecuteTest,
                ArgumentNodeExecuteTest.getMethod('mock')
        )

        and:
        def context = new CommandExecutionContext(new CommandSender(), (s, p) -> true)
        context.addInput('invalid')

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.mock-message'

        and:
        (e.cause instanceof NumberFormatException)

        cleanup:
        ArgumentParsers.parsers.remove(Integer)
    }

    @SuppressWarnings('unused')
    static void mock() {

    }

}
