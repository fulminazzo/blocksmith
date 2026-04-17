//file:noinspection unused
package it.fulminazzo.blocksmith.command.visitor.tab

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import spock.lang.Specification

import java.lang.reflect.Parameter

class TabCompletionVisitorTest extends Specification {
    private static final CommandSenderWrapper commandSender = new MockCommandSenderWrapper(new CommandSender().addPermissions('blocksmith.universal'))

    private static final Parameter parameter1 = TabCompletionVisitorTest.getDeclaredMethod('tabComplete', String).parameters[0]
    private static final Parameter parameter2 = TabCompletionVisitorTest.getDeclaredMethod('tabComplete', boolean).parameters[0]

    private static volatile String printer

    void setup() {
        printer = null
    }

    void cleanup() {
        printer = null
    }

    def 'test that tabComplete with #arguments returns #expected'() {
        given:
        def node = newLiteral('give')

        def item = ArgumentNode.of('item', parameter1, false)
        node.addChild(item)

        def player = newLiteral('player')
        node.addChild(player)

        def playerArg = ArgumentNode.of('player', parameter1, false)
        player.addChild(playerArg)

        item = ArgumentNode.of('item', parameter1, false)
        playerArg.addChild(item)

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                arguments[0],
                *arguments.subList(1, arguments.size())
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual.sort() == expected.sort()

        where:
        arguments                                   || expected
        ['give']                                    || ['give']
        ['give', '']                                || ['player', '<item>']
        ['give', 'p']                               || ['player']
        ['give', 'P']                               || ['player']
        ['give', 'a']                               || ['<item>']
        ['give', 'player']                          || ['player']
        ['give', 'playera']                         || ['<item>']
        ['give', 'player', '']                      || ['<player>']
        ['give', 'player', 'Alex']                  || ['<player>']
        ['give', 'player', 'Alex', '']              || ['<item>']
        ['give', 'player', 'Alex', 'diamond_sword'] || ['<item>']
        ['invalid', 'p']                            || []
    }

    def 'test that tabComplete of greedy parameter with #arguments returns #expected'() {
        given:
        def node = newLiteral('msg')

        def player = ArgumentNode.of('player', parameter1, false)
        node.addChild(player)

        def message = ArgumentNode.of('message', parameter1, false)
        message.greedy = true
        player.addChild(message)

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                arguments[0],
                *arguments.subList(1, arguments.size())
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual.sort() == expected.sort()

        where:
        arguments                                                  || expected
        ['msg', 'Alex', '']                                        || ['<message>']
        ['msg', 'Alex', 'Hello']                                   || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!']                        || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,']              || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!']     || ['<message>']
        ['msg', 'Alex', 'Hello,', 'world!', 'Hello,', 'Mars!', ''] || ['<message>']
    }

    def 'test tabComplete with no children'() {
        given:
        def node = newLiteral('msg')

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'msg',
                'Fulminazzo', 'Hello', 'world'
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual == []
    }

    def 'test that tabComplete does not continue if sender has no permission'() {
        given:
        def node = newLiteral('msg')

        def player = ArgumentNode.of('player', parameter1, false)
        node.addChild(player)

        and:
        def commandSender = Mock(CommandSenderWrapper)
        commandSender.hasPermission(_) >> false

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'msg',
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual == []
    }

    def 'test that tabComplete does not continue if argument node is invalid'() {
        given:
        def node = ArgumentNode.of('boolean', parameter2, false)

        def player = ArgumentNode.of('player', parameter1, false)
        node.addChild(player)

        def message = ArgumentNode.of('message', parameter1, false)
        message.greedy = true
        player.addChild(message)

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                'invalid',
                'Fulminazzo', 'Hello', 'world'
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual == []
    }

    def 'test that tabComplete of argument node does not throw on last'() {
        given:
        def node = ArgumentNode.of('boolean', parameter2, false)

        and:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                argument,
        )

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual.sort() == expected.sort()

        where:
        argument || expected
        ''       || ['true', 'false']
        'fa'     || ['false']
        'false'  || ['false']
        'tr'     || ['true']
        'true'   || ['true']
    }

    def 'test that tabComplete does not throw if input is done'() {
        given:
        def visitor = new TabCompletionVisitor(
                Mock(ApplicationHandle),
                commandSender,
                '',
        )
        visitor.input.advanceCursor()

        when:
        def actual = visitor.tabComplete(node)

        then:
        actual == []

        where:
        node << [
                newLiteral('msg'),
                ArgumentNode.of('boolean', parameter2, false)
        ]
    }

    private static final LiteralNode newLiteral(final String... aliases) {
        def node = new LiteralNode(aliases)
        node.commandInfo = new CommandInfo(
                "Description for literal node ${aliases[0]}",
                new PermissionInfo(
                        'blocksmith',
                        'universal',
                        Permission.Grant.ALL
                )
        )
        return node
    }

    static void tabComplete(final String string) {
        //
    }

    static void tabComplete(final boolean value) {
        //
    }

}
