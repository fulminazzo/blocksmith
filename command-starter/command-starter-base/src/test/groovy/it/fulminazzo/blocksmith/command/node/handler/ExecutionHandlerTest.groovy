package it.fulminazzo.blocksmith.command.node.handler

import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.visitor.execution.ExecutionContext
import spock.lang.Specification

import java.time.Duration

class ExecutionHandlerTest extends Specification {
    private LiteralNode commandNode
    private ExecutionContext context

    private CommandExecutor executor
    private ExecutionHandler handler

    void setup() {
        commandNode = Mock(LiteralNode)
        commandNode.commandInfo >> {
            def info = Mock(CommandInfo)
            info.permission >> {
                def permission = Mock(PermissionInfo)
                permission.prefix >> 'blocksmith'
                permission.actualPermission >> 'command'
                return permission
            }
            return Optional.of(info)
        }

        def sender = Mock(CommandSenderWrapper)
        sender.id >> 0

        context = Mock(ExecutionContext)
        context.commandSender >> sender

        executor = Mock(CommandExecutor)
        handler = new ExecutionHandler(executor)
    }

    def 'test that execute works'() {
        given:
        handler.cooldown = cooldown

        when:
        handler.execute(commandNode, context)

        then:
        1 * executor.execute(context)

        where:
        cooldown << [null, Duration.ofSeconds(1)]
    }

    def 'test that execute works during cooldown if sender can bypass permission'() {
        given:
        handler.cooldown = Duration.ofSeconds(10)

        and:
        context.commandSender.hasPermission(_ as PermissionInfo) >> { a ->
            return a[0].permission == 'blocksmith.bypass.cooldown.command'
        }

        and:
        handler.cooldownManager.put(context.commandSender.id)

        when:
        handler.execute(commandNode, context)

        then:
        1 * executor.execute(context)
    }

    def 'test that execute throws CommandExecutionException during cooldown'() {
        given:
        handler.cooldown = Duration.ofSeconds(10)

        and:
        handler.cooldownManager.put(context.commandSender.id)

        when:
        handler.execute(commandNode, context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.command-on-cooldown'

        and:
        def arguments = e.arguments
        arguments.length == 1

        and:
        def argument = arguments[0]
        def time = argument.timeSupplier.get()
        time >= 0
        time <= 10_000
    }

}
