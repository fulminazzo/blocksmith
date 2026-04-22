package it.fulminazzo.blocksmith.command.argument

import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.message.argument.Placeholder
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class NumberArgumentParserTest extends Specification {
    private final NumberArgumentParser parser = new NumberArgumentParser<>(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::valueOf)

    def 'test that parse with valid argument does not throw'() {
        when:
        parser.parse(prepareVisitor('1'))

        then:
        noExceptionThrown()
    }

    def 'test that parse throws for invalid argument'() {
        when:
        parser.parse(prepareVisitor('a'))

        then:
        def e = thrown(ArgumentParseException)
        e.arguments.toList() == [
                Placeholder.of('argument', 'a'),
                Placeholder.of("min", Integer.MIN_VALUE),
                Placeholder.of("max", Integer.MAX_VALUE)
        ]
    }

    def 'test that getCompletions returns #expected for argument #argument'() {
        given:
        def visitor = prepareVisitor(argument)

        when:
        def actual = parser.getCompletions(visitor)

        then:
        actual == expected

        where:
        argument             || expected
        ''                   || (0..9).collect { it.toString() }
        '1'                  || (0..9).collect { "1$it".toString() }
        '12'                 || (0..9).collect { "12$it".toString() }
        '-1'                 || (0..9).collect { "-1$it".toString() }
        '-12'                || (0..9).collect { "-12$it".toString() }
        'a'                  || []
        "$Integer.MAX_VALUE" || []
        "$Integer.MIN_VALUE" || []
    }

    private InputVisitor<?, ? extends Exception> prepareVisitor(final @NotNull String argument) {
        def context = Mock(InputVisitor)
        context.input >> {
            def input = Mock(CommandInput)
            input.current >> argument
            return input
        }
        return context
    }

}
