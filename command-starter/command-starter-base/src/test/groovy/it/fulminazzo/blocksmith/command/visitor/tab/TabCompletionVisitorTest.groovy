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

    private static final Parameter parameter = TabCompletionVisitorTest.getDeclaredMethod('tabComplete', String).parameters[0]

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

        def item = ArgumentNode.of('item', parameter, false)
        node.addChild(item)

        def player = newLiteral('player')
        node.addChild(player)

        def playerArg = ArgumentNode.of('player', parameter, false)
        player.addChild(playerArg)

        item = ArgumentNode.of('item', parameter, false)
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
    }

    def 'test that tabComplete of greedy parameter with #arguments returns #expected'() {
        given:
        def node = newLiteral('msg')

        def player = ArgumentNode.of('player', parameter, false)
        node.addChild(player)

        def message = ArgumentNode.of('message', parameter, false)
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

}
