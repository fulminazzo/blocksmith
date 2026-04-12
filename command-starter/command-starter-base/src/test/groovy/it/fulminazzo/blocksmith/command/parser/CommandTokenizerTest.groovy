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

    def 'test that #method throws IllegalStateException if nothing has been read yet'() {
        given:
        def tokenizer = new CommandTokenizer('')

        when:
        tokenizer."$method"()

        then:
        thrown(IllegalStateException)

        where:
        method << ['getLastToken', 'getLastRead']
    }

    def 'test that IOException is rethrown as CommandParseException'() {
        given:
        final cause = new IOException('Mock IOException')
        def stream = Mock(InputStream)
        stream.read() >> {
            throw cause
        }

        and:
        def tokenizer = new CommandTokenizer(stream)

        when:
        tokenizer.next()

        then:
        def e = thrown(CommandParseException)
        e.cause == cause
    }

}
