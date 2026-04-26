package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandMessages
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Confirm
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

class CancelNodeTest extends Specification {
    private final PendingTaskManager<Object> confirmationManager = new PendingTaskManager<>()

    private LiteralNode parent
    private CancelNode node

    private CommandExecutionVisitor visitor

    void setup() {
        parent = new LiteralNode('root')
        parent.commandInfo = new CommandInfo(
                'command.root.description',
                new PermissionInfo('permission', 'root', Permission.Grant.ALL)
        )

        def annotation = Mock(Confirm)
        annotation.cancelAliases() >> ['cancel'].toArray()
        annotation.cancelDescription() >> 'description.cancel'
        annotation.cancelPermission() >> 'permission.cancel'

        node = new CancelNode(annotation, parent, confirmationManager)

        def sender = Mock(CommandSenderWrapper)
        sender.idImpl >> 1
        sender.player >> true

        visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
    }

    def 'test that normal execute works'() {
        given:
        def bool = new AtomicBoolean()

        and:
        confirmationManager.register(visitor.commandSender.id, 10_000, () -> bool.set(true))

        when:
        node.executor.orElseThrow().execute(parent, visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == CommandMessages.PENDING_ACTION_CANCELLED

        and:
        !bool.get()
    }

    def 'test that execute with expired throws'() {
        given:
        confirmationManager.register(visitor.commandSender.id, 1, () -> {})

        and:
        sleep(500)

        when:
        node.executor.orElseThrow().execute(parent, visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == CommandMessages.PENDING_ACTION_EXPIRED
    }

    def 'test that execute with no task throws'() {
        when:
        node.executor.orElseThrow().execute(parent, visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == CommandMessages.NO_PENDING_ACTION
    }

}
