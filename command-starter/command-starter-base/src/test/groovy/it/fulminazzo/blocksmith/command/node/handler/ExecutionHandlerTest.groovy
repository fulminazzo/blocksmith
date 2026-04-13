package it.fulminazzo.blocksmith.command.node.handler

import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutorService

class ExecutionHandlerTest extends Specification {
    private LiteralNode commandNode
    private CommandExecutionVisitor visitor

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

        visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender

        executor = Mock(CommandExecutor)
        handler = new ExecutionHandler(new Object(), Object.getMethod('equals', Object))
        Reflect.on(handler).set('executor', executor)
    }

    def 'test that execute works'() {
        given:
        handler.cooldown = cooldown

        when:
        handler.execute(commandNode, visitor)

        then:
        1 * executor.execute(visitor)

        where:
        cooldown << [null, Duration.ofSeconds(1)]
    }

    def 'test that execute works during cooldown if sender can bypass permission'() {
        given:
        handler.cooldown = Duration.ofSeconds(10)

        and:
        visitor.commandSender.hasPermission(_ as PermissionInfo) >> { a ->
            return a[0].permission == 'blocksmith.bypass.cooldown.command'
        }

        and:
        handler.cooldownManager.put(visitor.commandSender.id)

        when:
        handler.execute(commandNode, visitor)

        then:
        1 * executor.execute(visitor)
    }

    def 'test that execute throws CommandExecutionException during cooldown'() {
        given:
        handler.cooldown = Duration.ofSeconds(10)

        and:
        handler.cooldownManager.put(visitor.commandSender.id)

        when:
        handler.execute(commandNode, visitor)

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

    def 'test that execute with asynchronous set executes the command asynchronously'() {
        given:
        def executorService = Mock(ExecutorService)
        executorService.submit(_ as Runnable) >> { a ->
            a[0].run()
        }

        and:
        handler.setAsync(executorService, Duration.ofSeconds(1L))

        when:
        handler.execute(commandNode, visitor)

        then:
        1 * executor.execute(visitor)

        when:
        handler.unsetAsync().execute(commandNode, visitor)

        then:
        1 * executor.execute(visitor)
    }

    def 'test that setAsync of invalid timeout throws'() {
        when:
        handler.setAsync(Mock(ExecutorService), Duration.ofSeconds(-1))

        then:
        thrown(IllegalArgumentException)
    }

}
