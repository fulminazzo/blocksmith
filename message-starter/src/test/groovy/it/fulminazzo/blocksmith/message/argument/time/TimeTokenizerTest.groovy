package it.fulminazzo.blocksmith.message.argument.time

import spock.lang.Specification

import static it.fulminazzo.blocksmith.message.argument.time.TimeToken.*

class TimeTokenizerTest extends Specification {

    def 'test next works'() {
        given:
        def text = '(%years% {year|years})[ %months% {month|months}] something (%days% {day|days} )'

        and:
        def expected = [
                OPEN_PHARENTHESIS,
                PERCENTAGE, LITERAL, PERCENTAGE,
                ANYTHING_ELSE,
                OPEN_BRACE, LITERAL, PIPE, LITERAL, CLOSE_BRACE,
                CLOSE_PARENTHESIS,
                OPEN_BRACKET,
                ANYTHING_ELSE,
                PERCENTAGE, LITERAL, PERCENTAGE,
                ANYTHING_ELSE,
                OPEN_BRACE, LITERAL, PIPE, LITERAL, CLOSE_BRACE,
                CLOSE_BRACKET,
                ANYTHING_ELSE,
                LITERAL,
                ANYTHING_ELSE,
                OPEN_PHARENTHESIS,
                PERCENTAGE, LITERAL, PERCENTAGE,
                ANYTHING_ELSE,
                OPEN_BRACE, LITERAL, PIPE, LITERAL, CLOSE_BRACE,
                ANYTHING_ELSE,
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