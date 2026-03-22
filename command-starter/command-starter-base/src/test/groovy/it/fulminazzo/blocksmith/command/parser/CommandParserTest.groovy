package it.fulminazzo.blocksmith.command.parser


import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.*
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class CommandParserTest extends Specification {

    def 'test that parseExpression of #input returns #expected'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        def node = parser.parseExpression()

        then:
        node == expected

        where:
        input                  || expected
        '[test]'               || new ArgumentNode('test', String, true)
        '<test>'               || new ArgumentNode('test', String, false)
        '(first|second|third)' || new LiteralNode('first', 'second', 'third')
        'test'                 || new LiteralNode('test')
    }

    def 'test that parseExpression throws for non-optional argument after optional argument'() {
        given:
        def parser = newMockCommandParser('test')
        parser.optionalArgument = 'optional'

        when:
        parser.parseExpression()

        then:
        def e = thrown(CommandParseException)
        e.message == "Invalid input in command 'test': after declaring optional argument 'optional', all subsequent nodes MUST be of the same kind (optional arguments)"
    }

    def 'test that parseOptionalArgument works'() {
        given:
        def parser = newMockCommandParser('[test]')

        when:
        def node = parser.parseOptionalArgument()

        then:
        node == new ArgumentNode('test', String, true)
    }

    def 'test that parseMandatoryArgument works'() {
        given:
        def parser = newMockCommandParser('<test>')

        when:
        def node = parser.parseMandatoryArgument()

        then:
        node == new ArgumentNode('test', String, false)
    }

    def 'test that parseGeneralArgument throws for non-matched argument'() {
        given:
        def parser = newMockCommandParser('test')

        and:
        parser.parameterIndex++

        when:
        parser.parseGeneralArgument(false)

        then:
        thrown(CommandParseException)
    }

    def 'test that parseAliasesLiteral for #input returns #expected'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        def node = parser.parseAliasesLiteral()

        then:
        node == new LiteralNode(*expected)

        where:
        input                  || expected
        '(first)'              || ['first']
        '(first|second)'       || ['first', 'second']
        '(first|second|third)' || ['first', 'second', 'third']
    }

    def 'test that parseAliasesLiteral throws for #input'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        parser.parseAliasesLiteral()

        then:
        thrown(CommandParseException)

        where:
        input << [
                '(first',
                '(first!',
                '(first!second',
                '(first|second',
                '(first|second!'
        ]
    }

    def 'test that parseSimpleLiteral works'() {
        given:
        def parser = newMockCommandParser('test')

        when:
        def node = parser.parseSimpleLiteral()

        then:
        node == new LiteralNode('test')
    }

    private static CommandParser newMockCommandParser(final @NotNull String input) {
        def parser = new CommandParser(input,
                new CommandInfo(
                        '',
                        new PermissionInfo('', Permission.Default.ALL)
                ),
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('newMockCommandParser', String)
                ),
                0
        )
        parser.tokenizer.next()
        return parser
    }

}
