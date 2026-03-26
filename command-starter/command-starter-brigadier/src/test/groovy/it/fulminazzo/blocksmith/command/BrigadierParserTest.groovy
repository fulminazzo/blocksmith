package it.fulminazzo.blocksmith.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.RootCommandNode
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import spock.lang.Specification

class BrigadierParserTest extends Specification {

    def 'test parseChild of known argument type'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = ArgumentNode.newNode('argument', int, false)
        node.addChild(ArgumentNode.newNode('value', boolean, false))

        and:
        def builder = LiteralArgumentBuilder.literal('sentinel')

        and:
        def parser = new BrigadierParser(delegate)

        when:
        parser.parseChild(new LiteralNode('sentinel'), builder, node)

        and:
        def arguments = builder.arguments

        then:
        arguments.size() == 1

        and:
        def argument = arguments.first()
        (argument instanceof ArgumentCommandNode)
        argument.name == 'argument'
        (argument.type instanceof IntegerArgumentType)

        when:
        def children = argument.children

        then:
        children.size() == 1

        and:
        def child = children.first()
        (child instanceof ArgumentCommandNode)
        child.name == 'value'
        (child.type instanceof BoolArgumentType)
    }

    def 'test that executes calls on delegate with root node'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = Mock(LiteralNode)

        and:
        def source = new Object()
        def input = 'Hello, world! How are you?'
        def context = new CommandContext(
                source,
                input,
                [:],
                null,
                null,
                [],
                null,
                null,
                null,
                false
        )

        and:
        def parser = new BrigadierParser(delegate)

        when:
        def command = parser.executes(node).run(context)

        then:
        command == 1

        and:
        1 * delegate.execute(
                node,
                source,
                'Hello,',
                'world! How are you?'.split(' ')
        )
    }

    def 'test that getArgumentType of #node returns #expected'() {
        when:
        def actual = BrigadierParser.getArgumentType(node as ArgumentNode)

        then:
        actual == null ? expected == null : actual.class == expected

        where:
        node                                                           || expected
        ArgumentNode.newNode('number', Byte, false)                    || IntegerArgumentType
        ArgumentNode.newNode('number', Short, false)                   || IntegerArgumentType
        ArgumentNode.newNode('number', Integer, false)                 || IntegerArgumentType
        ArgumentNode.newNode('number', Long, false)                    || LongArgumentType
        ArgumentNode.newNode('number', Float, false)                   || FloatArgumentType
        ArgumentNode.newNode('number', Double, false)                  || DoubleArgumentType
        ArgumentNode.newNode('boolean', Boolean, false)                || BoolArgumentType
        ArgumentNode.newNode('string', String, false)                  || StringArgumentType
        ArgumentNode.newNode('number', Integer, false).setGreedy(true) || StringArgumentType
        ArgumentNode.newNode('string', String, false).setGreedy(true)  || StringArgumentType
        ArgumentNode.newNode('object', Object, false)                  || null
    }

}
