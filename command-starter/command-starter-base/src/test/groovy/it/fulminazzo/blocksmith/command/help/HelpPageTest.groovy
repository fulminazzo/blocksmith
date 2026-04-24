package it.fulminazzo.blocksmith.command.help

import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import spock.lang.Specification

import java.lang.reflect.Parameter

class HelpPageTest extends Specification {
    private LiteralNode node

    private HelpPage page

    void setup() {
        node = newLiteralNode('message', 'msg', 'm')

        def parameter = Mock(Parameter)
        parameter.type >> String
        node.addChild(ArgumentNode.of('player', parameter, false))

        node.addChild(newLiteralNode('actionbar'))
        node.addChild(newLiteralNode('title'))
        node.addChild(newLiteralNode('bossbar'))
        node.addChild(newLiteralNode('help'))
        node.addChild(newLiteralNode('silent'))
        node.addChild(newLiteralNode('bb'))
        node.addChild(newLiteralNode('ab'))

        page = new HelpPage(
                HelpPage.CommandData.builder()
                        .name(node.name)
                        .description(node.commandInfo.description)
                        .permission(node.commandInfo.permission)
                        .usage(node.usage)
                        .build(),
                node.children
                        .findAll { it instanceof LiteralNode }
                        .collect {
                            HelpPage.CommandData.builder()
                                    .name(it.name)
                                    .description(it.commandInfo.description)
                                    .permission(it.commandInfo.permission)
                                    .usage(it.usage)
                                    .build()
                        }
        )
    }

    def 'test that getSubcommandsPages with #subcommands returns #expected'() {
        given:
        def page = Mock(HelpPage)
        page.getSubcommandsPages(_, _) >> {
            callRealMethod()
        }
        page.getExecutableSubcommands(_) >> {
            subcommands == 0
                    ? []
                    : (1..subcommands).collect { Mock(HelpPage.CommandData) }
        }

        and:
        def sender = Mock(CommandSenderWrapper)

        when:
        def pages = page.getSubcommandsPages(sender, 3)

        then:
        pages == expected

        where:
        subcommands || expected
        0           || 0
        1           || 1
        2           || 1
        3           || 1
        4           || 2
        5           || 2
        6           || 2
        7           || 3
    }

    def 'test that getExecutableSubcommands returns only subcommands which the sender has permission for'() {
        given:
        def expected = page.subcommands[0]

        and:
        def sender = Mock(CommandSenderWrapper)
        sender.hasPermission(_) >> { a ->
            a[0] == expected.permission
        }

        when:
        def subcommands = page.getExecutableSubcommands(sender)

        then:
        subcommands == [expected]
    }

    def 'test that create creates correct page'() {
        when:
        def actual = HelpPage.create(node)

        then:
        actual == page
    }

    private static LiteralNode newLiteralNode(final String... aliases) {
        def node = new LiteralNode(aliases)
        node.commandInfo = new CommandInfo(
                "description.${aliases[0]}",
                new PermissionInfo(
                        null,
                        "permission.${aliases[0]}",
                        Permission.Grant.ALL
                )
        )
        return node
    }

}
