package it.fulminazzo.blocksmith.command.visitor

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class CommandInputTest extends Specification {

    def 'test that CommandInput correctly returns all arguments'() {
        given:
        def arguments = ['Hello', 'world', 'My', 'name', 'is', 'Alex']

        and:
        def input = new CommandInput().addInput(*arguments)

        expect:
        def count = 0
        while (!input.done) {
            def expected = arguments[count]
            def actual = input.current

            assert expected == actual

            input.advanceCursor()
            count++
        }

        and:
        input.rawInput == arguments.join(' ')
    }

    def 'test that mergeRemaining correctly merges remaining input'() {
        given:
        def input = new CommandInput()
        input.input.addAll(['Hello', 'world', 'My', 'name', 'is', 'Alex'])

        and:
        Reflect.on(input).set('current', 2)

        and:
        def expected = 'My name is Alex'

        expect:
        input.mergeRemaining() == expected

        and:
        input.current == expected
    }

    def 'test that setCurrent correctly overwrites the current argument'() {
        given:
        def input = new CommandInput()
        input.input.addAll(['Hello'])

        and:
        input.current = 'world'

        expect:
        input.current == 'world'
    }

    def 'test that peek works'() {
        given:
        def input = new CommandInput()
        input.input.addAll(['Hello', 'world'])

        expect:
        input.peek() == 'world'
    }

    def 'test that isLast works'() {
        given:
        def input = new CommandInput()

        when:
        input.input.add('Hello')

        then:
        input.last

        when:
        input.input.add('Hello')

        then:
        !input.last
    }

}
