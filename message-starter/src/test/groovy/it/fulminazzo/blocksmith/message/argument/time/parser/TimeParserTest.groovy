package it.fulminazzo.blocksmith.message.argument.time.parser

import it.fulminazzo.blocksmith.message.argument.time.node.ArgumentNode
import it.fulminazzo.blocksmith.message.argument.time.node.LiteralNode
import it.fulminazzo.blocksmith.structure.Pair
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class TimeParserTest extends Specification {

    def 'test that parse works'() {
        given:
        def parser = new TimeParser('(!%years% {year|years})[ %months% {month|months}] ' +
                '(%days% {day|days}) this should totally be ignored[! %hours% {hour|hours}] ' +
                '(%minutes% {minute|minutes})')

        and:
        def expected = new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.YEARS, 'year', 'years', true)
                .addChild(new ArgumentNode(' %unit% %name%', ArgumentNode.TimeUnit.MONTHS, 'month', 'months', false).setOptional(true))
                .addChild(new LiteralNode(' '))
                .addChild(new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.DAYS, 'day', 'days', false))
                .addChild(new LiteralNode(' this should totally be ignored'))
                .addChild(new ArgumentNode(' %unit% %name%', ArgumentNode.TimeUnit.HOURS, 'hour', 'hours', true).setOptional(true))
                .addChild(new LiteralNode(' '))
                .addChild(new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.MINUTES, 'minute', 'minutes', false))

        when:
        def actual = parser.parse()

        then:
        actual == expected
    }

    def 'test that parseOptionalArgument works'() {
        given:
        def parser = newMockTimeParser("[${full ? '!' : ''}%years% {year|years}]")

        when:
        def actual = parser.parseOptionalArgument()

        then:
        actual == new ArgumentNode(
                '%unit% %name%',
                ArgumentNode.TimeUnit.YEARS,
                'year',
                'years',
                full
        ).setOptional(true)

        where:
        full << [true, false]
    }

    def 'test that parseAlwaysShownArgument works'() {
        given:
        def parser = newMockTimeParser("(${full ? '!' : ''}%years% {year|years})")

        when:
        def actual = parser.parseAlwaysShownArgument()

        then:
        actual == new ArgumentNode(
                '%unit% %name%',
                ArgumentNode.TimeUnit.YEARS,
                'year',
                'years',
                full
        )

        where:
        full << [true, false]
    }

    def 'test that parseGeneralArgument automatically determines singular and plural if not given'() {
        given:
        def parser = newMockTimeParser("%years%)")

        and:
        def expected = new ArgumentNode(
                '%unit%',
                ArgumentNode.TimeUnit.YEARS,
                'y',
                'y',
                false
        )

        when:
        def actual = parser.parseGeneralArgument(TimeToken.CLOSE_PARENTHESIS)

        then:
        actual == expected
    }

    def 'test that parseGeneralArgument throws for input #input'() {
        given:
        def parser = newMockTimeParser(input)

        when:
        parser.parseGeneralArgument(TimeToken.CLOSE_PARENTHESIS)

        then:
        thrown(TimeParseException)

        where:
        input << [
                '',
                'invalid',
                '()',
                '( {year|years})',
                '(%years% {year|years} %years%)'
        ]
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

    def 'test that match throws for unexpected token'() {
        given:
        def parser = newMockTimeParser('Hello')

        when:
        parser.match(TimeToken.CLOSE_BRACE)

        then:
        thrown(TimeParseException)
    }

    private static TimeParser newMockTimeParser(final @NotNull String input) {
        def parser = new TimeParser(input)
        parser.tokenizer.next()
        return parser
    }

}
