package it.fulminazzo.blocksmith.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class BrigadierParserTest extends Specification {

    def 'test parse of full node'() {
        given:
        def delegate = Mock(CommandRegistry)

        and:
        def expectedTargets = ['player', 'receiver', 'target']

        and:
        def root = new LiteralNode('message')

        def targetNode = new LiteralNode(*expectedTargets)
        root.addChild(targetNode)

        def playerNode = ArgumentNode.newNode('player', Object, false)
        targetNode.addChild(playerNode)

        playerNode.addChild(ArgumentNode.newNode('message', String, false).setGreedy(true))

        and:
        def parser = new BrigadierParser(delegate)

        when:
        def node = parser.parse(root)

        then:
        (node instanceof LiteralCommandNode)
        node.literal == 'message'

        when:
        def targetLiterals = node.children.sort { it.name }

        then:
        targetLiterals.size() == expectedTargets.size()

        and:
        for (def i in 0..expectedTargets.size() - 1) {
            def target = targetLiterals[i]
            assert (target instanceof LiteralCommandNode)
            assert target.literal == expectedTargets[i]

            def children = target.children
            assert children.size() == 1

            def player = children[0]
            assert (player instanceof ArgumentCommandNode)
            assert player.name == 'player'
            assert (player.type instanceof StringArgumentType)
            assert player.type.type == StringArgumentType.StringType.QUOTABLE_PHRASE
            assert player.customSuggestions != null

            children = player.children
            assert children.size() == 1

            def message = children[0]
            assert (message instanceof ArgumentCommandNode)
            assert message.name == 'message'
            assert (message.type instanceof StringArgumentType)
            assert message.type.type == StringArgumentType.StringType.GREEDY_PHRASE
            assert message.customSuggestions == null
        }
    }

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
        def argument = arguments[0]
        (argument instanceof ArgumentCommandNode)
        argument.name == 'argument'
        (argument.type instanceof IntegerArgumentType)

        when:
        def children = argument.children

        then:
        children.size() == 1

        and:
        def child = children[0]
        (child instanceof ArgumentCommandNode)
        child.name == 'value'
        (child.type instanceof BoolArgumentType)
    }

    def 'test parseChild of unknown argument type'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = ArgumentNode.newNode('argument', Object, false)
        node.addChild(ArgumentNode.newNode('value', boolean, false))

        and:
        def builder = LiteralArgumentBuilder.literal('sentinel')

        and:
        def source = new Object()
        def context = mockContext(source, input)

        and:
        def parser = new BrigadierParser(delegate)

        and:
        def root = new LiteralNode('sentinel')

        when:
        parser.parseChild(root, builder, node)

        and:
        def arguments = builder.arguments

        then:
        arguments.size() == 1

        and:
        def argument = arguments[0]
        (argument instanceof ArgumentCommandNode)
        argument.name == 'argument'
        (argument.type instanceof StringArgumentType)

        when:
        SuggestionProvider suggestion = argument.customSuggestions

        then:
        suggestion != null

        when:
        suggestion.getSuggestions(context, new SuggestionsBuilder(input, 0))

        then:
        1 * delegate.tabComplete(
                root,
                source,
                'argument',
                args.toArray(new String[args.size()])
        ) >> []

        when:
        def children = argument.children

        then:
        children.size() == 1

        and:
        def child = children[0]
        (child instanceof ArgumentCommandNode)
        child.name == 'value'
        (child.type instanceof BoolArgumentType)

        where:
        input                  || args
        'argument Hello '      || ['Hello', '']
        'argument Hello world' || ['Hello', 'world']
    }

    def 'test that executes calls on delegate with root node'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = Mock(LiteralNode)

        and:
        def source = new Object()
        def input = 'Hello, world! How are you?'
        def context = mockContext(source, input)

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

    private CommandContext mockContext(final @NotNull Object source, final @NotNull String input) {
        return new CommandContext(
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
    }

}
