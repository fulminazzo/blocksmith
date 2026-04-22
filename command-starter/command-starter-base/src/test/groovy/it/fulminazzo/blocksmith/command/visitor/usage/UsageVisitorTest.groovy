package it.fulminazzo.blocksmith.command.visitor.usage

import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.visitor.Visitor
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.lang.reflect.Parameter

class UsageVisitorTest extends Specification {
    private static final Parameter PARAMETER = Visitor.getMethod('visitCommandNode', CommandNode).parameters[0]

    private static final Visitor<String, ? extends Exception> SINGLE_VISITOR = new UsageVisitor.SimpleUsageVisitor()
    private static final List<CommandNode> TEST_NODES = [
            new LiteralNode('message', 'msg', 'm'),
            ArgumentNode.of('optional', PARAMETER, true),
            ArgumentNode.of('argument', PARAMETER, false),
            new LiteralNode('root')
    ]
    private static final Map<CommandNode, String> TEST_NODES_USAGES = TEST_NODES
            .collectEntries { [(it): it.accept(SINGLE_VISITOR)] }
            .sort { a, b -> a.key.name <=> b.key.name }

    private final Visitor<String, ? extends Exception> visitor = new UsageVisitor()

    void cleanup() {
        TEST_NODES.each {
            Reflect.on(it).set('parent', null)
            it.children.clear()
        }
    }

    def 'test that visit#type delegates to visitCommandNode'() {
        given:
        def visitor = Mock(UsageVisitor)
        visitor."visit${type.simpleName}"(_) >> {
            callRealMethod()
        }

        and:
        def node = Mock(type)

        when:
        visitor."visit${type.simpleName}"(node)

        then:
        1 * visitor.visitCommandNode(node)

        where:
        type << [LiteralNode, ArgumentNode]
    }

    /*
     * visitChildren
     */

    def 'test visitChildren with recursive children'() {
        given:
        def node = Mock(CommandNode)
        node.children >> [
                childrenSupplier()
        ].toSet()

        when:
        def usage = visitor.visitChildren(node)

        then:
        usage == expected

        where:
        childrenSupplier                   || expected
        ({
            def child1 = TEST_NODES[0]
            def child2 = TEST_NODES[1]
            child1.addChild(child2)
            return child1
        })                                 || " ${TEST_NODES_USAGES[TEST_NODES[0]]} ${TEST_NODES_USAGES[TEST_NODES[1]]}"
        ({
            def child1 = TEST_NODES[0]
            def child2 = TEST_NODES[1]
            def child3 = TEST_NODES[2]
            child3.addChild(TEST_NODES[3])
            child1.addChild(child2)
            child1.addChild(child3)
            return child1
        })                                 ||
                " ${TEST_NODES_USAGES[TEST_NODES[0]]} ${TEST_NODES_USAGES[TEST_NODES[2]]}<dark_gray>|</dark_gray>${TEST_NODES_USAGES[TEST_NODES[1]]}"
    }

    def 'test that visitChildren of multiple children returns all the children then stops'() {
        given:
        def node = Mock(CommandNode)
        node.children >> TEST_NODES.toSet().sort { a, b -> a.name <=> b.name }

        when:
        def usage = visitor.visitChildren(node)

        then:
        usage == ' ' + TEST_NODES_USAGES.values()
                .join('<dark_gray>|</dark_gray>')
    }

    def 'test that visitChildren of one child returns its usage'() {
        given:
        def node = Mock(CommandNode)
        node.children >> [
                child
        ].toSet()

        when:
        def usage = visitor.visitChildren(node)

        then:
        usage == ' ' + TEST_NODES_USAGES[child]

        where:
        child << TEST_NODES
    }

    def 'test that visitChildren with no children returns empty'() {
        given:
        def node = Mock(CommandNode)
        node.children >> [].toSet()

        when:
        def usage = visitor.visitChildren(node)

        then:
        usage == ''
    }

    def 'test that visitParentNode returns all usages of parent nodes correctly'() {
        given:
        def node = Mock(CommandNode)
        def parent1 = TEST_NODES[0]
        def parent2 = TEST_NODES[1]
        def parent3 = TEST_NODES[2]
        def parent4 = TEST_NODES[3]

        and:
        parent4.addChild(parent3).addChild(parent2).addChild(parent1)
        node.parent >> parent1

        when:
        def usage = visitor.visitParentNode(node)

        then:
        usage == TEST_NODES_USAGES[parent4] + ' ' +
                TEST_NODES_USAGES[parent3] + ' ' +
                TEST_NODES_USAGES[parent2] + ' ' +
                TEST_NODES_USAGES[parent1] + ' '
    }

}
