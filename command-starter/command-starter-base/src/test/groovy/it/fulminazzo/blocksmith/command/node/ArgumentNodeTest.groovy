//file:noinspection GrMethodMayBeStatic
//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import it.fulminazzo.blocksmith.validation.ValidationException
import it.fulminazzo.blocksmith.validation.annotation.Matches
import spock.lang.Specification

import java.lang.reflect.Parameter

class ArgumentNodeTest extends Specification {

    /*
     * tryAdvanceCursor
     */

    def 'test that tryAdvanceCursor of #arguments and #defaultValue returns #expected'() {
        given:
        def node = newArgumentNode(String)
        node.greedy = greedy
        node.defaultValue = defaultValue

        and:
        def visitor = Mock(Visitor)
        def input = new CommandInput()
        input.addInput(*arguments)
        visitor.input >> input

        when:
        def actual = node.tryAdvanceCursor(visitor)

        then:
        actual == expected

        where:
        arguments          | greedy | defaultValue || expected
        []                 | false  | null         || false
        []                 | true   | null         || false
        []                 | false  | 'tmp'        || true
        []                 | true   | 'tmp'        || true
        ['']               | false  | null         || true
        ['']               | true   | null         || true
        ['Hello']          | false  | null         || true
        ['Hello']          | true   | null         || true
        ['Hello', 'world'] | false  | null         || true
        ['Hello', 'world'] | true   | null         || true
    }

    /*
     * parseCurrent
     */

    def 'test that parseCurrent with custom supplier correctly parses #expected'() {
        given:
        def node = newArgumentNode(String)
        def supplier = new CompletionsSupplier(
                this,
                ArgumentNodeTest.getDeclaredMethod('completions')
        )
        node.completionsSupplier = supplier

        and:
        def visitor = Mock(Visitor)
        visitor.input >> {
            def input = new CommandInput()
            input.addInput(expected.contains(' ') ? "'$expected'" : expected)
            return input
        }

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == expected

        where:
        expected << ['yes', 'no', 'Hello, world!']
    }

    def 'test that parseCurrent throws ArgumentParseException if input does not match custom supplier completions'() {
        given:
        def node = newArgumentNode(boolean)
        def supplier = new CompletionsSupplier(
                this,
                ArgumentNodeTest.getDeclaredMethod('completions')
        )
        node.completionsSupplier = supplier

        and:
        def visitor = Mock(Visitor)
        visitor.input >> {
            def input = new CommandInput()
            input.addInput('invalid')
            return input
        }

        when:
        node.parseCurrent(visitor)

        then:
        thrown(ArgumentParseException)
    }

    def 'test that parseCurrent with no input and #defaultValue returns default value'() {
        given:
        def node = newArgumentNode(boolean)
        if (defaultValue != null) node.defaultValue = defaultValue.toString()

        and:
        def visitor = Mock(Visitor)
        visitor.input >> new CommandInput()

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == defaultValue

        where:
        defaultValue << [true, false, null]
    }

    def 'test that parseCurrent throws ValidationException on invalid value'() {
        given:
        def node = newArgumentNode(String)

        and:
        def visitor = Mock(Visitor)
        visitor.input >> {
            def input = new CommandInput()
            input.addInput('#@$%^&*')
            return input
        }

        when:
        node.parseCurrent(visitor)

        then:
        thrown(ValidationException)
    }

    def 'test that parseCurrent correctly parses greedy argument'() {
        given:
        def node = newArgumentNode(String)
        node.greedy = true

        and:
        def input = new CommandInput()
        input.addInput('Hello', 'world')

        and:
        def visitor = Mock(Visitor)
        visitor.input >> input

        when:
        def value = node.parseCurrent(visitor)

        then:
        value == 'Hello world'
    }

    def 'test that parseCurrent correctly parses #expected'() {
        given:
        def node = newArgumentNode(boolean)

        and:
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
        expected << [true, false]
    }


    def 'test that matches always returns true'() {
        expect:
        newArgumentNode(String).matches('')
    }

    def 'test that accept calls on visitArgumentNode'() {
        given:
        def node = newArgumentNode(boolean)

        and:
        def visitor = Mock(Visitor)

        when:
        node.accept(visitor)

        then:
        1 * visitor.visitArgumentNode(node)
    }

    def 'test that getCompletions with custom supplier returns provider'() {
        given:
        def node = newArgumentNode(boolean)
        def supplier = new CompletionsSupplier(
                this,
                ArgumentNodeTest.getDeclaredMethod('completions')
        )
        node.completionsSupplier = supplier

        and:
        def visitor = Mock(Visitor)

        expect:
        node.getCompletions(visitor).sort() == supplier.get().sort()
    }

    def 'test that getCompletions returns correct completions'() {
        given:
        def node = newArgumentNode(boolean)

        and:
        def visitor = Mock(Visitor)

        expect:
        node.getCompletions(visitor).sort() == ['true', 'false'].sort()
    }

    def 'test that getCompletions replaces String ArgumentParser name'() {
        given:
        def node = newArgumentNode(String)

        and:
        def visitor = Mock(Visitor)

        expect:
        node.getCompletions(visitor) == ['<argument>']
    }

    def 'test that of of #type returns #expected'() {
        given:
        def parameter = Mock(Parameter)
        parameter.type >> type

        when:
        def node = ArgumentNode.of('argument', parameter, false)

        then:
        node.class == expected

        where:
        type      || expected
        byte      || NumberArgumentNode
        Byte      || NumberArgumentNode
        short     || NumberArgumentNode
        Short     || NumberArgumentNode
        int       || NumberArgumentNode
        Integer   || NumberArgumentNode
        long      || NumberArgumentNode
        Long      || NumberArgumentNode
        float     || NumberArgumentNode
        Float     || NumberArgumentNode
        double    || NumberArgumentNode
        Double    || NumberArgumentNode
        char      || ArgumentNode
        Character || ArgumentNode
        boolean   || ArgumentNode
        Boolean   || ArgumentNode
        String    || ArgumentNode
        Object    || ArgumentNode
    }

    private ArgumentNode newArgumentNode(final Class<?> type) {
        def method = ArgumentNodeTest.getDeclaredMethod('execute', type)
        return ArgumentNode.of('argument', method.parameters[0], false)
    }

    /*
     * TEST METHODS
     */

    private List<String> completions() {
        return ['yes', 'no', 'Hello, world!']
    }

    private void execute(boolean value) {
        // test method
    }

    private void execute(@Matches('[^#@]+') String value) {
        // test method
    }

}
