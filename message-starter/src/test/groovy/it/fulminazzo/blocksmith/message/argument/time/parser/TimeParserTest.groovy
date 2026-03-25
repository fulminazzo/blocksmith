package it.fulminazzo.blocksmith.message.argument.time.parser

import it.fulminazzo.blocksmith.structure.Pair
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class TimeParserTest extends Specification {

    def 'test that parseSingularAndPlural works'() {
        given:
        def parser = newMockTimeParser('{year|years}')

        when:
        def node = parser.parseSingularAndPlural()

        then:
        node == Pair.of('year', 'years')
    }

    private static TimeParser newMockTimeParser(final @NotNull String input) {
        def parser = new TimeParser(input)
        parser.tokenizer.next()
        return parser
    }
    
}
