package it.fulminazzo.blocksmith.message.argument.time.parser

import it.fulminazzo.blocksmith.message.argument.time.node.ArgumentNode
import it.fulminazzo.blocksmith.structure.Pair
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class TimeParserTest extends Specification {

    def 'test that parseOptionalArgument works'() {
        given:
        def parser = newMockTimeParser("[${full ? '!' : ''}%years% {year|years}]")

        and:
        def expected = new ArgumentNode(
                '%unit% %name%',
                ArgumentNode.TimeUnit.YEARS,
                'year',
                'years',
                full
        )
        expected.optional = true

        when:
        def actual = parser.parseOptionalArgument()

        then:
        actual == expected

        where:
        full << [true, false]
    }

    def 'test that parseShownArgument works'() {
        given:
        def parser = newMockTimeParser("(${full ? '!' : ''}%years% {year|years})")

        and:
        def expected = new ArgumentNode(
                '%unit% %name%',
                ArgumentNode.TimeUnit.YEARS,
                'year',
                'years',
                full
        )

        when:
        def actual = parser.parseShownArgument()

        then:
        actual == expected

        where:
        full << [true, false]
    }

    def 'test that parseUnitPlaceholder returns #unit'() {
        given:
        def parser = newMockTimeParser("%$unit.name%")

        when:
        def actual = parser.parseUnitPlaceholder()

        then:
        actual == unit

        where:
        unit << ArgumentNode.TimeUnit.values()
    }

    def 'test that parseUnitPlaceholder throws for invalid unit'() {
        given:
        def parser = newMockTimeParser('%invalid%')

        when:
        parser.parseUnitPlaceholder()

        then:
        thrown(TimeParseException)
    }

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
