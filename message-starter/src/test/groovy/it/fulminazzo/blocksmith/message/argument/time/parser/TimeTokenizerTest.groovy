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

}