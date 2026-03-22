package it.fulminazzo.blocksmith.command.node

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import spock.lang.Specification

import java.lang.reflect.Method

class CommandNodeTest extends Specification {
    private static final @NotNull Method method = CommandNodeTest.getDeclaredMethod('prepareNode', String, ExecutionInfo, Collection)
    private static final @NotNull ExecutionInfo executionInfo = new ExecutionInfo(CommandNodeTest, method)
    private static final @NotNull CommandNode fullMockNode = prepareNode('first',
            executionInfo,
            [
                    new MockNode('first_child'),
                    new MockNode('second_child')
            ]
    )

    def 'test that addChild correctly adds new child'() {
        given:
        def child = new MockNode('third_child')

        and:
        CommandNode node = fullMockNode.clone()

        when:
        def actual = node.addChild(child)

        then:
        actual == child
    }

    def 'test that addChild returns existing but updated child'() {
        given:
        def child = new MockNode('second_child')
        child.executionInfo = executionInfo

        and:
        CommandNode node = fullMockNode.clone()
        node.children.each { it.addChild(new MockNode('inner')) }

        and:
        def expected = prepareNode('second_child',
                executionInfo,
                [new MockNode('inner')]
        )

        when:
        def actual = node.addChild(child)

        then:
        actual == expected
    }

    def 'test that merge of #first and #second returns expected'() {
        when:
        first.merge(second)

        then:
        first == fullMockNode

        where:
        first || second
        fullMockNode                   || prepareNode('first', null, [])
        prepareNode('first', null, []) || fullMockNode
        prepareNode('first', executionInfo, [new MockNode('first_child')]) ||
                prepareNode('first', null, [new MockNode('second_child')])
        prepareNode('first', null, [new MockNode('first_child')]) ||
                prepareNode('first', executionInfo, [new MockNode('second_child')])
    }

    private static @NotNull CommandNode prepareNode(final @NotNull String name,
                                                    final @Nullable ExecutionInfo executionInfo,
                                                    final @NotNull Collection<CommandNode> children) {
        def node = new MockNode(name)
        node.children.addAll(children)
        node.executionInfo = executionInfo
        return node
    }

}
