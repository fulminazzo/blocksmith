//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParser
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

class NumberArgumentNodeTest extends Specification {
    private NumberArgumentNode<?> node = newArgumentNode(int)
            .min(2)
            .max(8)

    /*
     * parseCurrent
     */

    def 'test that parseCurrent correctly parses #expected'() {
        given:
        def visitor = Mock(Visitor)
        visitor.input >> {
            def input = new CommandInput()
            input.addInput(expected.toString())
            return input
        }

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == expected

        where:
        expected << (2..8)
    }

    def 'test that parseCurrent does not throw for null value'() {
        given:
        def node = Spy(NumberArgumentNode, constructorArgs: [
                'argument',
                NumberArgumentNodeTest.getDeclaredMethod('execute', int).parameters[0],
                false
        ])
        node.parser >> {
            return Mock(ArgumentParser)
        }

        and:
        def visitor = Mock(Visitor)
        visitor.input >> new CommandInput()

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == null
    }

    def 'test that parseCurrent throws for #number'() {
        given:
        def visitor = Mock(Visitor)
        visitor.input >> {
            def input = new CommandInput()
            input.addInput(number.toString())
            return input
        }

        when:
        node.parseCurrent(visitor)

        then:
        thrown(ArgumentParseException)

        where:
        number << [-1, 0, 1, 9, 10, 21, 1024]
    }


    def 'test that getCompletions removes invalid numbers'() {
        given:
        def visitor = Mock(Visitor)
        visitor.input >> new CommandInput().addInput('')

        when:
        def completions = node.getCompletions(visitor)

        then:
        completions == (2..8).collect { "$it" }
    }

    private NumberArgumentNode newArgumentNode(final Class<? extends Number> type) {
        def method = NumberArgumentNodeTest.getDeclaredMethod('execute', type)
        return ArgumentNode.of('argument', method.parameters[0], false)
    }

    /*
     * TEST METHODS
     */

    private void execute(final int value) {
        // test method
    }

}
