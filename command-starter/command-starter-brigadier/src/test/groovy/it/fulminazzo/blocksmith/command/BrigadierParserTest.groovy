package it.fulminazzo.blocksmith.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import it.fulminazzo.blocksmith.command.annotation.Confirm
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Parameter

class BrigadierParserTest extends Specification {

    def 'test parse of full node'() {
        given:
        def sender = Mock(CommandSenderWrapper)
        sender.actualSender >> new Object()

        and:
        def delegate = Mock(CommandRegistry)
        delegate.wrapSender(_) >> sender

        and:
        def expectedTargets = ['player', 'receiver', 'target']

        and:
        def root = new LiteralNode('message')

        def targetNode = new LiteralNode(*expectedTargets)
        targetNode.commandInfo = new CommandInfo(
                'description',
                new PermissionInfo(null, 'permission', Permission.Grant.ALL)
        )
        root.addChild(targetNode)

        def playerNode = newArgumentNode('player', Object)
        targetNode.addChild(playerNode)

        playerNode.addChild(newArgumentNode('message', String).setGreedy(true))

        and:
        def parser = new BrigadierParser(delegate)

        when:
        def node = parser.parse(root.name, root)

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

        and:
        def target = targetLiterals[0]
        def requirement = target.requirement
        requirement != null

        when:
        requirement.test(sender.actualSender)

        then:
        1 * sender.hasPermission(_)
    }

    def 'test parse of confirmation node'() {
        given:
        def delegate = Mock(CommandRegistry)

        and:
        def confirm = Mock(Confirm)
        confirm.confirmWord() >> 'confirm'
        confirm.cancelWord() >> 'cancel'

        and:
        def root = new LiteralNode('delete')
        root.setConfirmationInfo(confirm)

        and:
        def parser = new BrigadierParser(delegate)

        when:
        def node = parser.parse(root.name, root)

        then:
        def children = node.children
        children.size() == 2
        children.find { it.name == 'confirm' } != null
        children.find { it.name == 'cancel' } != null
    }

    def 'test parseChild of known argument type'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = newArgumentNode('argument', int)
        node.addChild(newArgumentNode('value', boolean))

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
        def node = newArgumentNode('argument', Object)
        node.addChild(newArgumentNode('value', boolean))

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
        input                   || args
        'argument Hello '       || ['Hello', '']
        '/argument Hello '      || ['Hello', '']
        'argument Hello world'  || ['Hello', 'world']
        '/argument Hello world' || ['Hello', 'world']
    }

    def 'test that executes calls on delegate with root node'() {
        given:
        def delegate = Mock(CommandRegistry)
        def node = Mock(LiteralNode)

        and:
        def source = new Object()
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

        where:
        input << [
                'Hello, world! How are you?',
                '/Hello, world! How are you?'
        ]
    }

    def 'test that getArgumentType of #node returns #expected'() {
        when:
        def actual = BrigadierParser.getArgumentType(node as ArgumentNode)

        then:
        actual == null ? expected == null : actual.class == expected

        where:
        node                                               || expected
        newArgumentNode('number', Byte)                    || IntegerArgumentType
        newArgumentNode('number', Short)                   || IntegerArgumentType
        newArgumentNode('number', Integer)                 || IntegerArgumentType
        newArgumentNode('number', Long)                    || LongArgumentType
        newArgumentNode('number', Float)                   || FloatArgumentType
        newArgumentNode('number', Double)                  || DoubleArgumentType
        newArgumentNode('boolean', Boolean)                || BoolArgumentType
        newArgumentNode('string', String)                  || StringArgumentType
        newArgumentNode('number', Integer).setGreedy(true) || StringArgumentType
        newArgumentNode('string', String).setGreedy(true)  || StringArgumentType
        newArgumentNode('object', Object)                  || null
    }

    def 'test that getArgumentType of #type returns #expected'() {
        when:
        def actual = BrigadierParser.getArgumentType(type)

        then:
        actual == null ? expected == null : actual.class == expected

        where:
        type    || expected
        Byte    || IntegerArgumentType
        Short   || IntegerArgumentType
        Integer || IntegerArgumentType
        Long    || LongArgumentType
        Float   || FloatArgumentType
        Double  || DoubleArgumentType
        Boolean || BoolArgumentType
        String  || StringArgumentType
        Object  || null
    }

    def 'test that getArgumentType of node with custom completions supplier returns null'() {
        given:
        def node = newArgumentNode('string', String)
        node.completionsSupplier = Mock(CompletionsSupplier)

        when:
        def actual = BrigadierParser.getArgumentType(node)

        then:
        actual == null
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

    private ArgumentNode<?> newArgumentNode(final String name, final Class<?> type) {
        def parameter = Mock(Parameter)
        parameter.type >> type
        return ArgumentNode.of(name, parameter, false)
    }

}
