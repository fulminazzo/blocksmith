package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.help.HelpPage
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor
import it.fulminazzo.blocksmith.message.Messenger
import net.kyori.adventure.text.Component
import spock.lang.Specification

class HelpPageNodeTest extends Specification {
    private LiteralNode commandNode
    private HelpPageNode node

    private InputVisitor<?, ? extends Exception> visitor

    void setup() {
        commandNode = new LiteralNode('command')
        commandNode.commandInfo = new CommandInfo(
                'command.description',
                new PermissionInfo(null, 'command.permission', Permission.Grant.ALL)
        )

        def helpNode = Mock(HelpNode)
        helpNode.name >> 'help'
        helpNode.commandInfo >> new CommandInfo(
                'command.help.description',
                new PermissionInfo(null, 'command.help.permission', Permission.Grant.ALL)
        )
        helpNode.usage >> '/command help'
        helpNode.helpCommandName >> 'help'
        helpNode.children >> []
        commandNode.addChild(helpNode)

        node = Spy(HelpPageNode, constructorArgs: [commandNode])

        def sender = Mock(CommandSenderWrapper)
        sender.hasPermission(_) >> { a ->
            return a[0].grant == Permission.Grant.ALL
        }

        visitor = Mock(InputVisitor)
        visitor.input >> new CommandInput().addInput('')
        visitor.commandSender >> sender
    }

    def 'test that executor renders all pages'() {
        given:
        def application = Mock(ApplicationHandle)
        application.messenger >> {
            def messenger = Mock(Messenger)
            messenger.getComponentOrElse(_, _, _) >> Component.empty()
            return messenger
        }

        and:
        visitor.commandSender.locale >> Locale.ITALY

        and:
        def visitor = new CommandExecutionVisitor(
                application,
                visitor.commandSender,
                'command',
                'help', '1'
        ).addArgument(1)
        visitor.input.advanceCursor().advanceCursor().advanceCursor()

        when:
        node.executor.get().execute(commandNode, visitor)

        then:
        10 * visitor.commandSender.sendMessage(_ as Component)
    }

    def 'test that parseCurrent works'() {
        given:
        visitor.input.current = '1'

        when:
        def actual = node.parseCurrent(visitor)

        then:
        actual == 1
    }

    def 'test that getCompletions return only valid pages'() {
        given:
        node.helpPage >> {
            def page = Mock(HelpPage)
            page.getSubcommandsPages(_, _) >> 5
            return page
        }

        when:
        def completions = node.getCompletions(visitor)

        then:
        completions == (1..5).collect { "$it" }
    }

}
