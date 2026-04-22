package it.fulminazzo.blocksmith.command.visitor.usage

import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

class SimpleUsageVisitorTest extends Specification {
    private Visitor<String, ? extends Exception> visitor = new UsageVisitor.SimpleUsageVisitor()

    def 'test that visit of ArgumentNode with #optional and #greedy returns #expected'() {
        given:
        def node = Mock(ArgumentNode)
        node.type >> String
        node.name >> 'message'
        node.optional >> optional
        node.greedy >> greedy
        node.accept(_) >> {
            callRealMethod()
        }

        when:
        def usage = node.accept(visitor)

        then:
        usage == expected

        where:
        optional | greedy || expected
        false    | false  || '<dark_gray><</dark_gray><white>message</white><dark_gray>></dark_gray>'
        false    | true   || '<dark_gray><</dark_gray><white>message...</white><dark_gray>></dark_gray>'
        true     | false  || '<dark_gray>[</dark_gray><white>message</white><dark_gray>]</dark_gray>'
        true     | true   || '<dark_gray>[</dark_gray><white>message...</white><dark_gray>]</dark_gray>'
    }

    def 'test that visit of LiteralNode returns correct usage'() {
        given:
        def node = new LiteralNode('message', 'give', 'say')

        when:
        def usage = node.accept(visitor)

        then:
        usage == '<red>give</red><dark_gray>|</dark_gray><red>message</red><dark_gray>|</dark_gray><red>say</red>'
    }

    def 'test that visit of generic CommandNode throws'() {
        when:
        visitor.visitCommandNode(Mock(CommandNode))

        then:
        thrown(UnsupportedOperationException)
    }

}
