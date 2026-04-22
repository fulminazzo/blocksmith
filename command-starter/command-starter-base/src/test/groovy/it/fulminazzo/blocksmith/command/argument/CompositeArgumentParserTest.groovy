package it.fulminazzo.blocksmith.command.argument

import it.fulminazzo.blocksmith.command.CommandMessages
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import spock.lang.Specification

class CompositeArgumentParserTest extends Specification {
    private static final List<String> numbers = (0..9).collect { it.toString() }

    private ArgumentParser<Object> parser

    private CommandInput input
    private InputVisitor<?, ? extends Exception> visitor

    void setup() {
        parser = new CompositeArgumentParser<>(Integer, Double, Boolean)

        input = new CommandInput()
        visitor = Mock(InputVisitor)
        visitor.input >> input
    }

    def 'test that initializing CompositeArgumentParser with no types throws'() {
        when:
        new CompositeArgumentParser<>(new Class<?>[0])

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that initializing CompositeArgumentParser with no parsers throws'() {
        when:
        new CompositeArgumentParser<>(new ArgumentParser<?>[0])

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that parse of #argument returns #expected'() {
        given:
        input.addInput(argument)

        when:
        def actual = parser.parse(visitor)

        then:
        actual == expected

        where:
        argument || expected
        '1'      || 1.intValue()
        '1.0'    || 1.0d
        '1.2'    || 1.2d
        'true'   || true
        'false'  || false
    }

    def 'test that parse with invalid argument throws last exception'() {
        given:
        input.addInput('invalid')

        when:
        parser.parse(visitor)

        then:
        def e = thrown(ArgumentParseException)
        e.message == CommandMessages.INVALID_BOOLEAN
    }

    def 'test that getCompletions works'() {
        given:
        input.addInput(argument)

        when:
        def actual = parser.getCompletions(visitor)

        then:
        actual == expected

        where:
        argument || expected
        ''       || [*numbers, 'true', 'false']
        '0'      || [*numbers.collect { "0$it" }, 'true', 'false']
        '0123'   || [*numbers.collect { "0123$it" }, 'true', 'false']
        'tr'     || ['true', 'false']
        'true'   || ['true', 'false']
        'fa'     || ['true', 'false']
        'false'  || ['true', 'false']
    }

}
