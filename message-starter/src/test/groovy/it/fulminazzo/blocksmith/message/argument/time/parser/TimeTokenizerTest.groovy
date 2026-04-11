package it.fulminazzo.blocksmith.message.argument.time.parser

import spock.lang.Specification

import static it.fulminazzo.blocksmith.message.argument.time.parser.TimeToken.*

class TimeTokenizerTest extends Specification {

    def 'test next works'() {
        given:
        def text = '(%years% {year|years})[ %months% {month|months}] something (%days% {day|days} )'

        and:
        def expected = [
                OPEN_PHARENTHESIS,
                PERCENTAGE, TEXT, PERCENTAGE,
                TEXT,
                OPEN_BRACE, TEXT, PIPE, TEXT, CLOSE_BRACE,
                CLOSE_PARENTHESIS,
                OPEN_BRACKET,
                TEXT,
                PERCENTAGE, TEXT, PERCENTAGE,
                TEXT,
                OPEN_BRACE, TEXT, PIPE, TEXT, CLOSE_BRACE,
                CLOSE_BRACKET,
                TEXT,
                OPEN_PHARENTHESIS,
                PERCENTAGE, TEXT, PERCENTAGE,
                TEXT,
                OPEN_BRACE, TEXT, PIPE, TEXT, CLOSE_BRACE,
                TEXT,
                CLOSE_PARENTHESIS
        ]

        and:
        def tokenizer = new TimeTokenizer(text)

        when:
        def actual = []
        TimeToken curr
        do {
            curr = tokenizer.next()
            if (curr != EOF) actual.add(curr)
        } while (curr != EOF)

        then:
        actual == expected
    }

    def 'test that #method throws IllegalStateException if nothing has been read yet'() {
        given:
        def tokenizer = new TimeTokenizer('')

        when:
        tokenizer."$method"()

        then:
        thrown(IllegalStateException)

        where:
        method << ['getLastToken', 'getLastRead']
    }

    def 'test that IOException is rethrown as TimeParseException'() {
        given:
        final cause = new IOException('Mock IOException')
        def stream = Mock(InputStream)
        stream.read() >> {
            throw cause
        }

        and:
        def tokenizer = new TimeTokenizer(stream)

        when:
        tokenizer.next()

        then:
        def e = thrown(TimeParseException)
        e.cause == cause
    }

}