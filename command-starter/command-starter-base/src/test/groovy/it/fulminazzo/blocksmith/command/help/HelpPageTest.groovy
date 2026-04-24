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

        page = new HelpPage(
                HelpPage.CommandData.builder()
                        .name(node.name)
                        .description(node.commandInfo.description)
                        .permission(node.commandInfo.permission)
                        .usage(node.usage)
                        .build(),
                [
                        HelpPage.CommandData.builder()
                                .name(node.children[0].name)
                                .description(node.children[0].commandInfo.description)
                                .permission(node.children[0].commandInfo.permission)
                                .usage(node.children[0].usage)
                                .build(),
                        HelpPage.CommandData.builder()
                                .name(node.children[2].name)
                                .description(node.children[2].commandInfo.description)
                                .permission(node.children[2].commandInfo.permission)
                                .usage(node.children[2].usage)
                                .build(),
                ]
        )
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
