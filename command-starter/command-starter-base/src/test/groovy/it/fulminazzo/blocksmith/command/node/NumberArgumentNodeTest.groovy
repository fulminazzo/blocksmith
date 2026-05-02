//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParser
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import spock.lang.Specification

import java.lang.reflect.Parameter

class NumberArgumentNodeTest extends Specification {
    private NumberArgumentNode<? extends Number> node = newArgumentNode(int)
            .min(2)
            .max(8)

    def 'test that initialize with #type sets min and max to #min and #max'() {
        given:
        def parameter = Mock(Parameter)
        parameter.type >> type

        when:
        def node = new NumberArgumentNode('number', parameter, false)

        then:
        node.min == min
        node.max == max

        where:
        type    || min               | max
        byte    || Byte.MIN_VALUE    | Byte.MAX_VALUE
        Byte    || Byte.MIN_VALUE    | Byte.MAX_VALUE
        short   || Short.MIN_VALUE   | Short.MAX_VALUE
        Short   || Short.MIN_VALUE   | Short.MAX_VALUE
        int     || Integer.MIN_VALUE | Integer.MAX_VALUE
        Integer || Integer.MIN_VALUE | Integer.MAX_VALUE
        long    || Long.MIN_VALUE    | Long.MAX_VALUE
        Long    || Long.MIN_VALUE    | Long.MAX_VALUE
        float   || -Float.MAX_VALUE  | Float.MAX_VALUE
        Float   || -Float.MAX_VALUE  | Float.MAX_VALUE
        double  || -Double.MAX_VALUE | Double.MAX_VALUE
        Double  || -Double.MAX_VALUE | Double.MAX_VALUE
    }

    /*
     * parseCurrent
     */

    def 'test that parseCurrent correctly parses #expected'() {
        given:
        def visitor = Mock(InputVisitor)
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
        def visitor = Mock(InputVisitor)
        visitor.input >> new CommandInput()

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == null
    }

    def 'test that parseCurrent throws for #number'() {
        given:
        def visitor = Mock(InputVisitor)
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


    def 'test that getCompletions allows for custom completions'() {
        given:
        def visitor = Mock(InputVisitor)
        visitor.input >> new CommandInput().addInput('')

        and:
        def supplier = Mock(CompletionsSupplier)
        supplier.get(visitor) >> ['PI']
        node.completionsSupplier = supplier

        when:
        def completions = node.getCompletions(visitor)

        then:
        completions == ['PI']
    }

    def 'test that getCompletions removes invalid numbers'() {
        given:
        def visitor = Mock(InputVisitor)
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
