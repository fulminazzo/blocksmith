package it.fulminazzo.blocksmith.command.parser

import spock.lang.Specification

import static it.fulminazzo.blocksmith.command.parser.CommandToken.*

class CommandTokenizerTest extends Specification {

    def 'test next works'() {
        given:
        def text = 'clan member <player> (promote|rankup) <rank> [reason]'

        and:
        def expected = [LITERAL, SPACE,
                        LITERAL, SPACE,
                        LOWER_THAN, LITERAL, GREATER_THAN, SPACE,
                        OPEN_PHARENTHESIS, LITERAL, PIPE, LITERAL, CLOSE_PARENTHESIS, SPACE,
                        LOWER_THAN, LITERAL, GREATER_THAN, SPACE,
                        OPEN_BRACKET, LITERAL, CLOSE_BRACKET
        ]

        and:
        def tokenizer = new CommandTokenizer(text)

        when:
        def actual = []
        CommandToken curr
        do {
            curr = tokenizer.next()
            if (curr != EOF) actual.add(curr)
        } while (curr != EOF)

        then:
        actual == expected
    }

}
