package it.fulminazzo.blocksmith.command.visitor.usage

import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.visitor.Visitor
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.lang.reflect.Parameter

class UsageVisitorTest extends Specification {
    private static final UsageStyle USAGE_STYLE = UsageStyle.get()
    private static final Parameter PARAMETER = Visitor.getMethod('visitCommandNode', CommandNode).parameters[0]

    private static final String NODE_SEPARATOR = UsageStyle.colorize(USAGE_STYLE.separator, USAGE_STYLE.punctuationColor)
    private static final String LITERAL_SEPARATOR = UsageStyle.colorize(USAGE_STYLE.literalSeparator, USAGE_STYLE.literalSeparatorColor)
    private static final String OPEN_BRACKET = UsageStyle.colorize('[', USAGE_STYLE.punctuationColor)
    private static final String CLOSE_BRACKET = UsageStyle.colorize(']', USAGE_STYLE.punctuationColor)
    private static final String LESS_THAN = UsageStyle.colorize('<', USAGE_STYLE.punctuationColor)
    private static final String GREATER_THAN = UsageStyle.colorize('>', USAGE_STYLE.punctuationColor)
    private static final String SLASH = UsageStyle.colorize('/', USAGE_STYLE.literalColor)

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

    def 'test that getUsage returns all the correct usages'() {
        given:
        def clan = new LiteralNode('clan', 'team', 'gang')
        def name = ArgumentNode.of('name', PARAMETER, false)
        clan.addChild(name)

        def help = new LiteralNode('help', '?')
        clan.addChild(help)

        def player = new LiteralNode('player')
        clan.addChild(player)
        def playerArg = ArgumentNode.of('player', PARAMETER, false)
        player.addChild(playerArg)

        def invite = new LiteralNode('invite')
        playerArg.addChild(invite)

        def promote = new LiteralNode('promote')
        playerArg.addChild(promote)
        def rank = ArgumentNode.of('rank', PARAMETER, true)
        promote.addChild(rank)

        def demote = new LiteralNode('demote')
        playerArg.addChild(demote)
        rank = ArgumentNode.of('rank', PARAMETER, true)
        demote.addChild(rank)
        def reason = ArgumentNode.of('reason', PARAMETER, true)
        reason.greedy = true
        demote.addChild(reason)

        def kick = new LiteralNode('kick')
        playerArg.addChild(kick)
        reason = ArgumentNode.of('reason', PARAMETER, false)
        reason.greedy = true
        kick.addChild(reason)

        and:
        def lit = { String s -> UsageStyle.colorize(s, USAGE_STYLE.literalColor) }
        def arg = { String s -> UsageStyle.colorize(s, USAGE_STYLE.defaultArgumentColor) }
        def optArg = { String s -> UsageStyle.colorize(s, USAGE_STYLE.defaultOptionalArgumentColor) }
        def greedy = { String s -> String.format(USAGE_STYLE.greedyArgumentFormat, s) }

        and:
        def clanUsage = "${SLASH}${lit('clan')}${LITERAL_SEPARATOR}${lit('gang')}${LITERAL_SEPARATOR}${lit('team')}"
        def playerUsage = "${lit('player')} ${LESS_THAN}${arg('player')}${GREATER_THAN}"
        def helpUsage = "${lit('?')}${LITERAL_SEPARATOR}${lit('help')}"

        expect:
        invite.accept(visitor) == "${clanUsage} ${playerUsage} ${lit('invite')}"

        and:
        kick.accept(visitor) == "${clanUsage} ${playerUsage} ${lit('kick')} " +
                "${LESS_THAN}${arg(greedy('reason'))}${GREATER_THAN}"

        and:
        promote.accept(visitor) == "${clanUsage} ${playerUsage} ${lit('promote')} " +
                "${OPEN_BRACKET}${optArg('rank')}${CLOSE_BRACKET}"

        and:
        demote.accept(visitor) == "${clanUsage} ${playerUsage} ${lit('demote')} " +
                "${OPEN_BRACKET}${optArg('rank')}${CLOSE_BRACKET}${NODE_SEPARATOR}" +
                "${OPEN_BRACKET}${optArg(greedy('reason'))}${CLOSE_BRACKET}"

        and:
        player.accept(visitor) == "${clanUsage} ${playerUsage} " +
                "${lit('demote')}${NODE_SEPARATOR}${lit('invite')}${NODE_SEPARATOR}" +
                "${lit('kick')}${NODE_SEPARATOR}${lit('promote')}"

        and:
        help.accept(visitor) == "${clanUsage} ${helpUsage}"

        and:
        clan.accept(visitor) == "${clanUsage} ${helpUsage}${NODE_SEPARATOR}" +
                "${LESS_THAN}${arg('name')}${GREATER_THAN}${NODE_SEPARATOR}${lit('player')}"
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
                " ${TEST_NODES_USAGES[TEST_NODES[0]]} ${TEST_NODES_USAGES[TEST_NODES[2]]}${NODE_SEPARATOR}${TEST_NODES_USAGES[TEST_NODES[1]]}"
    }

    def 'test that visitChildren of multiple children returns all the children then stops'() {
        given:
        def node = Mock(CommandNode)
        node.children >> TEST_NODES.toSet().sort { a, b -> a.name <=> b.name }

        when:
        def usage = visitor.visitChildren(node)

        then:
        usage == ' ' + TEST_NODES_USAGES.values().join(NODE_SEPARATOR)
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
