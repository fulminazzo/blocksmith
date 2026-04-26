package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler
import it.fulminazzo.blocksmith.command.visitor.usage.UsageVisitor
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.mockito.Mockito
import spock.lang.Specification

import java.lang.reflect.Method
import java.lang.reflect.Parameter

class CommandNodeTest extends Specification {
    private static final Method method = CommandNodeTest.getDeclaredMethod('prepareNode', String, ExecutionHandler, Collection)
    private static final ExecutionHandler executor = new ExecutionHandler(CommandNodeTest, method)
    private static final CommandNode fullMockNode = prepareNode('first',
            executor,
            [
                    new MockNode('first_child'),
                    new MockNode('second_child')
            ]
    )

    private CommandNode node = fullMockNode

    def 'test that addChild correctly adds new child'() {
        given:
        def child = new MockNode('third_child')

        when:
        def actual = node.addChild(child)

        then:
        actual == child
    }

    def 'test that addChild returns existing but updated child'() {
        given:
        def child = new MockNode('second_child')
        child.executor = executor

        and:
        node.children.each { it.addChild(new MockNode('inner')) }

        and:
        def expected = prepareNode('second_child',
                executor,
                [new MockNode('inner')]
        )

        when:
        def actual = node.addChild(child)

        then:
        actual == expected
    }

    def 'test that addChild sets parent on newly added child'() {
        given:
        def node = new MockNode('parent')
        def child = new MockNode('child')

        when:
        node.addChild(child)

        then:
        child.parent == node
    }

    def 'test that addChild sets parent when merging with existing child'() {
        given:
        def node = new MockNode('parent')
        def existing = new MockNode('child')
        node.addChild(existing)

        and:
        def duplicate = new MockNode('child')

        when:
        def returned = node.addChild(duplicate)

        then:
        returned.parent == node
    }

    def 'test that addChild increases children count when child is new'() {
        given:
        def sizeBefore = node.children.size()

        when:
        node.addChild(new MockNode('brand_new_child'))

        then:
        node.children.size() == sizeBefore + 1
    }

    def 'test that addChild does not increase children count when child already exists'() {
        given:
        def sizeBefore = node.children.size()

        when:
        node.addChild(new MockNode('first_child'))

        then:
        node.children.size() == sizeBefore
    }

    def 'test that getUsage calls on UsageVisitor'() {
        given:
        def mock = Mockito.mockStatic(UsageVisitor)

        and:
        def node = Mock(CommandNode)
        node.usage >> {
            callRealMethod()
        }

        when:
        node.usage

        then:
        mock.verify({ UsageVisitor.generateUsage(node) })

        cleanup:
        mock.close()
    }

    def 'test that getCommandNode returns itself when node is a LiteralNode'() {
        given:
        def literal = new LiteralNode('command')

        expect:
        literal.getCommandNode() == literal
    }

    def 'test that getCommandNode returns parent LiteralNode for a non-literal child'() {
        given:
        def literal = new LiteralNode('command')
        def arg = newMockArgumentNode('arg')
        literal.addChild(arg)

        expect:
        arg.getCommandNode() == literal
    }

    def 'test that getCommandNode returns null when no LiteralNode ancestor exists'() {
        given:
        def orphanArg = newMockArgumentNode('orphan')

        expect:
        orphanArg.getCommandNode() == null
    }

    def 'test that getCommandNode walks up multiple levels to find LiteralNode'() {
        given:
        def literal = new LiteralNode('promote')
        def argPlayer = newMockArgumentNode('player')
        def argRank = newMockArgumentNode('rank')
        literal.addChild(argPlayer)
        argPlayer.addChild(argRank)

        expect:
        argRank.getCommandNode() == literal
    }

    def 'test that getCommandNode on nested LiteralNode returns the nearest one'() {
        given:
        def root = new LiteralNode('clan')
        def sub = new LiteralNode('promote')
        def arg = newMockArgumentNode('rank')
        root.addChild(sub)
        sub.addChild(arg)

        expect:
        arg.getCommandNode() == sub
    }

    def 'test that isExecutable returns true when executor is set'() {
        given:
        def node = new MockNode('node')
        node.executor = executor

        expect:
        node.isExecutable()
    }

    def 'test that isExecutable returns false when executor is not set'() {
        given:
        def node = new MockNode('node')

        expect:
        !node.isExecutable()
    }

    def 'test that isExecutable returns false after executor is cleared'() {
        given:
        def node = new MockNode('node')
        node.executor = executor

        when:
        node.executor = null

        then:
        !node.isExecutable()
    }

    def 'test that getOptionalArgument returns null when there are no children'() {
        given:
        def node = new MockNode('node')

        expect:
        node.getOptionalArgument() == null
    }

    def 'test that getOptionalArgument returns null when no ArgumentNode child is optional'() {
        given:
        def node = new MockNode('node')
        def arg = newMockArgumentNode('arg', false)
        node.addChild(arg)

        expect:
        node.getOptionalArgument() == null
    }

    def 'test that getOptionalArgument returns null when children are all LiteralNodes'() {
        given:
        def node = new MockNode('node')
        node.addChild(new LiteralNode('sub'))

        expect:
        node.getOptionalArgument() == null
    }

    def 'test that getOptionalArgument returns the optional ArgumentNode'() {
        given:
        def node = new MockNode('node')
        def arg = newMockArgumentNode('arg', true)
        node.addChild(arg)

        expect:
        node.getOptionalArgument() == arg
    }

    def 'test that getOptionalArgument returns optional ArgumentNode when mixed children exist'() {
        given:
        def node = new MockNode('node')
        node.addChild(new LiteralNode('sub'))
        def arg = newMockArgumentNode('arg', true)
        node.addChild(arg)

        expect:
        node.getOptionalArgument() == arg
    }

    def 'test that getChild returns null when there are no children'() {
        given:
        def node = new MockNode('node')

        expect:
        node.getChild('anything') == null
    }

    def 'test that getChild returns null when no child matches'() {
        expect:
        node.getChild('nonexistent') == null
    }

    def 'test that getChild returns matching LiteralNode child'() {
        given:
        def node = new MockNode('node')
        def literal = new LiteralNode('sub')
        node.addChild(literal)

        expect:
        node.getChild('sub') == literal
    }

    def 'test that getChild prioritizes LiteralNode over ArgumentNode for same token'() {
        given:
        def node = new MockNode('node')
        def literal = new LiteralNode('rankup', 'promote')
        def arg = newMockArgumentNode('promote')
        node.addChild(arg)
        node.addChild(literal)

        expect:
        node.getChild('promote') == literal
    }

    def 'test that getChild falls back to ArgumentNode when no LiteralNode matches'() {
        given:
        def node = new MockNode('node')
        def arg = newMockArgumentNode('player')
        node.addChild(new LiteralNode('other'))
        node.addChild(arg)

        expect:
        node.getChild('Steve') == arg
    }

    def 'test that getFirstChild returns null when there are no children'() {
        given:
        def node = new MockNode('node')

        expect:
        node.getFirstChild() == null
    }

    def 'test that getFirstChild returns the only child when there is one'() {
        given:
        def node = new MockNode('node')
        def child = new MockNode('only')
        node.addChild(child)

        expect:
        node.getFirstChild() == child
    }

    def 'test that getFirstChild returns the alphabetically first child'() {
        given:
        def node = new MockNode('node')
        node.addChild(new MockNode('zebra'))
        node.addChild(new MockNode('alpha'))
        node.addChild(new MockNode('middle'))

        expect:
        node.getFirstChild().getName() == 'alpha'
    }

    def 'test that merge of #first and #second returns expected'() {
        when:
        first.merge(second)

        then:
        first == fullMockNode

        where:
        first                                                         || second
        fullMockNode                                                  || prepareNode('first', null, [])
        prepareNode('first', null, [])                                || fullMockNode
        prepareNode('first', executor, [new MockNode('first_child')]) ||
                prepareNode('first', null, [new MockNode('second_child')])
        prepareNode('first', null, [new MockNode('first_child')])     ||
                prepareNode('first', executor, [new MockNode('second_child')])
    }

    def 'test equals and hashCode'() {
        expect:
        (first == second) == equalsExpected

        and:
        !equalsExpected || first.hashCode() == second.hashCode()

        where:
        first                              | second                             || equalsExpected
        new MockNode('same')               | new MockNode('same')               || true
        new MockNode('first')              | new MockNode('second')             || false
        new MockNode('same')               | new LiteralNode('same')            || false
        new MockNode('first')              | new LiteralNode('second')          || false
        withExecutor(new MockNode('same')) | withExecutor(new MockNode('same')) || true
        withExecutor(new MockNode('same')) | new MockNode('same')               || false
        new LiteralNode('help')            | new LiteralNode('help')            || true
        new LiteralNode('help', '?')       | new LiteralNode('help')            || false
        new MockNode('help')               | new LiteralNode('help')            || false
        new MockNode('help')               | 'help'                             || false
    }

    private ArgumentNode newMockArgumentNode(final @NotNull String name) {
        return newMockArgumentNode(name, false)
    }

    private ArgumentNode newMockArgumentNode(final @NotNull String name, final boolean optional) {
        def parameter = Mock(Parameter)
        parameter.type >> String
        return ArgumentNode.of(
                name,
                parameter,
                optional
        )
    }

    private static CommandNode prepareNode(final @NotNull String name,
                                           final @Nullable ExecutionHandler executionHandler,
                                           final @NotNull Collection<CommandNode> children) {
        def node = new MockNode(name)
        node.children.addAll(children)
        node.executor = executionHandler
        return node
    }

    private static <T extends CommandNode> T withExecutor(final @NotNull T node) {
        node.executor = executor
        return node
    }

}
