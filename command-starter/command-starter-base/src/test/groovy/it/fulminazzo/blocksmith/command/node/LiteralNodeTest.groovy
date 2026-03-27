//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.annotation.Permission
import spock.lang.Specification

import java.time.Duration

class LiteralNodeTest extends Specification {

    def 'test that LiteralNode of #literals and #token returns correct value for matches'() {
        given:
        def node = new LiteralNode(*literals)

        when:
        def actual = node.matches(token)

        then:
        actual == expected

        where:
        literals                                | token    || expected
        ['FIRST', 'second']                     | 'first'  || true
        ['first', 'second']                     | 'SECOND' || true
        ['first', 'second']                     | 'third'  || false
        ['first', '           sEcOnD         '] | 'Second' || true
    }

    def 'test that LiteralNode initialization throws with no literals'() {
        when:
        new LiteralNode()

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that clone correctly clones node'() {
        given:
        def first = new LiteralNode('first')
        first.commandInfo = commandInfo
        first.executionInfo = executionInfo
        first.cooldown = cooldown
        if (child != null) first.addChild(child)

        and:
        def expected = new LiteralNode('second')
        expected.commandInfo = commandInfo
        expected.executionInfo = executionInfo
        expected.cooldown = cooldown
        if (child != null) expected.addChild(child)

        when:
        def second = first.clone('second')

        then:
        second == expected

        where:
        commandInfo                                                       | executionInfo | cooldown                                 | child
        null                                                              |
                null                                                                      | null                                     | null
        new CommandInfo('', new PermissionInfo('', Permission.Grant.ALL)) |
                null                                                                      | null                                     | null
        new CommandInfo('', new PermissionInfo('', Permission.Grant.ALL)) |
                new ExecutionInfo(LiteralNodeTest, LiteralNodeTest.getMethod('mock'))     | null                                     | null
        new CommandInfo('', new PermissionInfo('', Permission.Grant.ALL)) |
                new ExecutionInfo(LiteralNodeTest, LiteralNodeTest.getMethod('mock'))     | Duration.ofSeconds(1)                    | null
        new CommandInfo('', new PermissionInfo('', Permission.Grant.ALL)) |
                new ExecutionInfo(LiteralNodeTest, LiteralNodeTest.getMethod('mock'))     | Duration.ofSeconds(1)                    | new LiteralNode('child')
    }

    static void mock() {

    }

}
