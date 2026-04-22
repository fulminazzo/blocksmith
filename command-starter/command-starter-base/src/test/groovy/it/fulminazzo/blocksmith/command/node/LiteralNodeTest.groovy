//file:noinspection GrDeprecatedAPIUsage
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Confirm
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.handler.ConfirmationHandler
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.command.visitor.Visitor
import spock.lang.Specification

import java.lang.reflect.Parameter

class LiteralNodeTest extends Specification {

    private LiteralNode node = new LiteralNode('test', 'command')

    void setup() {
        node.commandInfo = new CommandInfo(
                '',
                new PermissionInfo('', '', Permission.Grant.ALL, true),
                true
        )
    }

    def 'test that constructor correctly adds aliases'() {
        when:
        def node = new LiteralNode('message', 'msg', 'm')

        then:
        node.name == 'message'
        node.aliases.sort() == ['message', 'msg', 'm'].sort()
    }

    def 'test that constructor throws if no literal is provided'() {
        when:
        new LiteralNode()

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that getCommandInfo throws if not initialized'() {
        given:
        def node = new LiteralNode('test')

        when:
        node.commandInfo

        then:
        thrown(IllegalStateException)
    }

    def 'test that setConfirmationInfo works'() {
        given:
        def info = Mock(Confirm)

        when:
        node.confirmationInfo = info

        then:
        def confirmationHandler = node.confirmationHandler
        confirmationHandler != null
        confirmationHandler.confirmationInfo == info
    }

    def 'test that setConfirmationInfo supports null'() {
        when:
        node.confirmationInfo = null

        then:
        noExceptionThrown()

        and:
        node.confirmationHandler == null
    }

    def 'test that accept calls on visitLiteralNode'() {
        given:
        def visitor = Mock(Visitor)

        when:
        node.accept(visitor)

        then:
        1 * visitor.visitLiteralNode(node)
    }

    def 'test that merge with existing CommandInfo calls on merge'() {
        given:
        def child1 = new LiteralNode('child1')
        child1.commandInfo = new CommandInfo(
                'child1.description',
                new PermissionInfo(null, 'child1.permission', Permission.Grant.NONE)
        )
        def child2 = new LiteralNode('child2')
        child2.commandInfo = new CommandInfo(
                'child2.description',
                new PermissionInfo(null, 'child2.permission', Permission.Grant.NONE)
        )

        and:
        def first = new LiteralNode('test')
        first.commandInfo = Mock(CommandInfo)
        first.addChild(child1)

        and:
        def second = new LiteralNode('command')
        second.commandInfo = new CommandInfo(
                'description',
                new PermissionInfo(null, 'permission', Permission.Grant.ALL)
        )
        second.addChild(child2)

        when:
        def node = first.merge(second)

        then:
        node == first
        node.aliases.sort() == ['test', 'command'].sort()

        and:
        1 * first.commandInfo.merge(second.commandInfo)

        and:
        def children = node.children
        children.contains(child1)
        children.contains(child2)
        children.size() == 2
    }

    def 'test that merge with not-existing CommandInfo sets'() {
        given:
        def child1 = new LiteralNode('child1')
        child1.commandInfo = new CommandInfo(
                'child1.description',
                new PermissionInfo(null, 'child1.permission', Permission.Grant.NONE)
        )
        def child2 = new LiteralNode('child2')
        child2.commandInfo = new CommandInfo(
                'child2.description',
                new PermissionInfo(null, 'child2.permission', Permission.Grant.NONE)
        )

        and:
        def first = new LiteralNode('test')
        first.addChild(child1)

        and:
        def second = new LiteralNode('command')
        second.commandInfo = new CommandInfo(
                'description',
                new PermissionInfo(null, 'permission', Permission.Grant.ALL)
        )
        second.addChild(child2)

        when:
        def node = first.merge(second)

        then:
        node == first
        node.aliases.sort() == ['test', 'command'].sort()

        and:
        node.commandInfo == second.commandInfo

        and:
        def children = node.children
        children.contains(child1)
        children.contains(child2)
        children.size() == 2
    }

    def 'test that merge of non-literal works'() {
        given:
        def child1 = new LiteralNode('child1')
        child1.commandInfo = new CommandInfo(
                'child1.description',
                new PermissionInfo(null, 'child1.permission', Permission.Grant.NONE)
        )
        def child2 = new LiteralNode('child2')
        child2.commandInfo = new CommandInfo(
                'child2.description',
                new PermissionInfo(null, 'child2.permission', Permission.Grant.NONE)
        )

        and:
        def first = new LiteralNode('test')
        first.addChild(child1)

        and:
        def parameter = Mock(Parameter)
        parameter.type >> String
        def second = new MockNode('command')
        second.addChild(child2)

        when:
        def node = first.merge(second)

        then:
        node == first
        node.aliases.sort() == ['test'].sort()

        and:
        def children = node.children
        children.contains(child1)
        children.contains(child2)
        children.size() == 2
    }

    def 'test that matches of #token returns #expected'() {
        expect:
        node.matches(token) == expected

        where:
        token     || expected
        'test'    || true
        'TEST'    || true
        'Test'    || true
        'TeST'    || true
        'command' || true
        'COMMAND' || true
        'CoMmANd' || true
        'invalid' || false
        'INVALID' || false
        'InvaLID' || false
        'InVaLiD' || false
    }

    def 'test that getCompletions returns #expected if #hasPermission and #requiresConfirmation'() {
        given:
        def sender = Mock(CommandSenderWrapper)
        sender.hasPermission(_) >> hasPermission

        and:
        if (requiresConfirmation) {
            def confirmationHandler = Mock(ConfirmationHandler)
            confirmationHandler.getCompletions(_) >> ['confirm', 'cancel']
            node.confirmationHandler = confirmationHandler
        }

        and:
        def visitor = Mock(InputVisitor)
        visitor.commandSender >> sender

        when:
        def actual = node.getCompletions(visitor)

        then:
        actual.sort() == expected.sort()

        where:
        hasPermission | requiresConfirmation || expected
        false         | false                || []
        true          | false                || ['test', 'command']
        false         | true                 || []
        true          | true                 || ['test', 'command', 'confirm', 'cancel']
    }

}
