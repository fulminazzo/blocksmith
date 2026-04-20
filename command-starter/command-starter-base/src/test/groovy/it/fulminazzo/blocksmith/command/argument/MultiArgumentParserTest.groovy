package it.fulminazzo.blocksmith.command.argument

import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

class MultiArgumentParserTest extends Specification {
    private static final List<String> numbers = (0..9).collect { it.toString() }

    private ArgumentParser<Position> parser

    private CommandInput input
    private Visitor<?, ? extends Exception> visitor

    void setup() {
        parser = new MultiArgumentParser<>(
                (l) -> new Position(l[0], l[1], l[2]),
                double, double, double
        )

        input = new CommandInput()
        visitor = Mock(Visitor)
        visitor.input >> input
    }

    def 'test that initializing MultiArgumentParser with no types throws'() {
        when:
        new MultiArgumentParser<>((l) -> null, new Class<?>[0])

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that parse works'() {
        given:
        input.addInput(arguments)

        when:
        def actual = parser.parse(visitor)

        then:
        actual == new Position(1, 2, 3)

        where:
        arguments << [
                ['1', '2', '3'],
                ['1', '2', '3', 'Hello'],
                ['1', '2', '3', 'Hello', 'world!'],
                ['1', '2', '3', 'Hello', 'world!', 'Goodbye', 'mars!']
        ]
    }

    def 'test that parse throws ArgumentParseException with arguments #arguments'() {
        given:
        input.addInput(arguments)

        when:
        parser.parse(visitor)

        then:
        def e = thrown(ArgumentParseException)
        e.message == 'error.not-enough-arguments'

        where:
        arguments << [
                [],
                ['1'],
                ['1', '2'],
        ]
    }

    def 'test that getCompletions of #arguments returns #expected'() {
        given:
        input.addInput(arguments)

        when:
        def actual = parser.getCompletions(visitor)

        then:
        actual == expected

        where:
        arguments             || expected
        // first
        ['']                  || numbers
        ['1']                 || numbers.collect { "1$it" }
        ['1234']              || numbers.collect { "1234$it" }
        ['a']                 || []
        // second
        ['1', '']             || numbers
        ['1', '2']            || numbers.collect { "2$it" }
        ['1', '234']          || numbers.collect { "234$it" }
        ['1', 'a']            || []
        ['a', '']             || []
        ['a', '2']            || []
        ['a', '234']          || []
        ['a', 'a']            || []
        // third
        ['1', '2', '']        || numbers
        ['1', '2', '3']       || numbers.collect { "3$it" }
        ['1', '2', '34']      || numbers.collect { "34$it" }
        ['1', '2', 'a']       || []
        ['1', 'a', '']        || []
        ['1', 'a', '3']       || []
        ['1', 'a', '34']      || []
        ['1', 'a', 'a']       || []
        ['a', '2', '']        || []
        ['a', '2', '3']       || []
        ['a', '2', '34']      || []
        ['a', '2', 'a']       || []
        ['a', 'a', '']        || []
        ['a', 'a', '3']       || []
        ['a', 'a', '34']      || []
        ['a', 'a', 'a']       || []
        // fourth
        ['1', '1', '3', '4']  || []
        ['1', '1', '34', '4'] || []
    }

}
