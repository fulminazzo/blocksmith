package it.fulminazzo.blocksmith.command.visitor

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class CommandInputTest extends Specification {

    def 'test that CommandInput correctly returns all arguments'() {
        given:
        def arguments = ['Hello,', 'world', 'My', 'name', 'is', 'Alex']

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

    def 'test that CommandInput correctly re-arranges quoted inputs'() {
        given:
        def input = new CommandInput()
                .addInput(input1)
                .addInput(input2)

        expect:
        input.input == expected

        where:
        input1                   | input2                                            || expected
        []                       | []                                                || []
        ['"Hello, world!"']      | []                                                || ['Hello, world!']
        ['"Hello,', 'world!"']   | []                                                || ['Hello, world!']
        ['"Hello, world!"']      | ['"Goodbye, Mars!"']                              || ['Hello, world!', 'Goodbye, Mars!']
        ['"Hello,', 'world!"']   | ['"Goodbye,', 'Mars!"']                           || ['Hello, world!', 'Goodbye, Mars!']
        []                       | ['"Hello, world!"']                               || ['Hello, world!']
        []                       | ['"Hello,', 'world!"']                            || ['Hello, world!']
        []                       | ['"Hello, world!"', '"Goodbye, Mars!"']           || ['Hello, world!', 'Goodbye, Mars!']
        []                       | ['"Hello,', 'world!"', '"Goodbye,', 'Mars!"']     || ['Hello, world!', 'Goodbye, Mars!']
        ['"Hello,']              | ['world!"']                                       || ['Hello, world!']
        ['"Hello,']              | ['world!"', '"Goodbye, Mars!"']                   || ['Hello, world!', 'Goodbye, Mars!']
        ['"Hello,']              | ['world!"', '"Goodbye,', 'Mars!"']                || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello, world!\'']    | []                                                || ['Hello, world!']
        ['\'Hello,', 'world!\''] | []                                                || ['Hello, world!']
        ['\'Hello, world!\'']    | ['\'Goodbye, Mars!\'']                            || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello,', 'world!\''] | ['\'Goodbye, Mars!\'']                            || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello, world!\'']    | ['\'Goodbye,', 'Mars!\'']                         || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello,', 'world!\''] | ['\'Goodbye,', 'Mars!\'']                         || ['Hello, world!', 'Goodbye, Mars!']
        []                       | ['\'Hello, world!\'']                             || ['Hello, world!']
        []                       | ['\'Hello,', 'world!\'']                          || ['Hello, world!']
        []                       | ['\'Hello, world!\'', '\'Goodbye, Mars!\'']       || ['Hello, world!', 'Goodbye, Mars!']
        []                       | ['\'Hello,', 'world!\'', '\'Goodbye,', 'Mars!\''] || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello,']             | ['world!\'']                                      || ['Hello, world!']
        ['\'Hello,']             | ['world!\'', '\'Goodbye, Mars!\'']                || ['Hello, world!', 'Goodbye, Mars!']
        ['\'Hello,']             | ['world!\'', '\'Goodbye,', 'Mars!\'']             || ['Hello, world!', 'Goodbye, Mars!']
    }

    def 'test that CommandInput correctly adds quoted input'() {
        given:
        def input = new CommandInput()
                .addInput('msg')
                .addInput('\'The').addInput('Diamond').addInput('Player\'')
                .addInput('"Hello,').addInput('friend!"')
                .addInput('CHAT')

        expect:
        input.input == ['msg', 'The Diamond Player', 'Hello, friend!', 'CHAT']
    }

    def 'test that mergeRemaining correctly merges remaining input'() {
        given:
        def input = new CommandInput()
        input.input.addAll(['Hello,', 'world', 'My', 'name', 'is', 'Alex'])

        and:
        Reflect.on(input).set('current', 2)

        and:
        def expected = 'My name is Alex'

        when:
        input.mergeRemaining()

        then:
        input.current == expected
    }

    def 'test that mergeRemaining does not throw nor edits original input if already advanced to end'() {
        given:
        def input = new CommandInput()
        def reflect = Reflect.on(input)
        final original = ['Hello,', 'world', 'My', 'name', 'is', 'Alex']
        input.input.addAll(original)

        and:
        reflect.set('current', 6)

        expect:
        input.done

        when:
        input.mergeRemaining()

        then:
        noExceptionThrown()

        and:
        input.done

        and:
        reflect.get('input').get() == original
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
        input.input.addAll(['Hello,', 'world'])

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

    def 'test toString works'() {
        given:
        def input = new CommandInput()

        when:
        input.toString()

        then:
        noExceptionThrown()
    }

}
