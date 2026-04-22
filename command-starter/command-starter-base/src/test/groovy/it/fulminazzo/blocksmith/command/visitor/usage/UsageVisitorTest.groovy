package it.fulminazzo.blocksmith.command.visitor.usage

import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

import java.lang.reflect.Parameter

class UsageVisitorTest extends Specification {
    private static final Parameter parameter = Visitor.getMethod('visitCommandNode', CommandNode).parameters[0]

    private final Visitor<String, ? extends Exception> visitor = new UsageVisitor()
    private final Visitor<String, ? extends Exception> singleVisitor = new UsageVisitor.SimpleUsageVisitor()

    def 'test that visitParentNode returns all usages of parent nodes correctly'() {
        given:
        def node = Mock(CommandNode)
        def parent1 = new LiteralNode('message', 'msg', 'm')
        def parent2 = ArgumentNode.of('optional', parameter, true)
        def parent3 = ArgumentNode.of('argument', parameter, false)
        def parent4 = new LiteralNode('root')

        and:
        parent4.addChild(parent3).addChild(parent2).addChild(parent1)
        node.parent >> parent1

        when:
        def usage = visitor.visitParentNode(node)

        then:
        usage == parent4.accept(singleVisitor) + ' ' +
                parent3.accept(singleVisitor) + ' ' +
                parent2.accept(singleVisitor) + ' ' +
                parent1.accept(singleVisitor) + ' '
    }

}
