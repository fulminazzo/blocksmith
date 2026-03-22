package it.fulminazzo.blocksmith.command.node

import spock.lang.Specification

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

}
