package it.fulminazzo.blocksmith.command.parser

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandInfo
import it.fulminazzo.blocksmith.command.node.ExecutionInfo
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class CommandParserTest extends Specification {

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
