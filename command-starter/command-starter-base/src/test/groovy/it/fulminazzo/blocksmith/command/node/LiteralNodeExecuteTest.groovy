package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import spock.lang.Specification

class LiteralNodeExecuteTest extends Specification {

    def 'test that validateInput does not throw with #commandInfo'() {
        given:
        def node = new LiteralNode('test')
        node.executionInfo = new ExecutionInfo(
                LiteralNodeExecuteTest,
                LiteralNodeExecuteTest.getMethod('mock')
        )
        node.commandInfo = commandInfo

        and:
        def context = new CommandExecutionContext(
                new CommandSender(),
                (s, p) -> p.permissionDefault == Permission.Default.OP
        )

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        where:
        commandInfo << [
                null,
                new CommandInfo('', new PermissionInfo('permission', Permission.Default.ALL)),
                new CommandInfo('', new PermissionInfo('permission', Permission.Default.OP))
        ]
    }

    def 'test that validateInput throws for no permission'() {
        given:
        def node = new LiteralNode('test')
        node.executionInfo = new ExecutionInfo(
                LiteralNodeExecuteTest,
                LiteralNodeExecuteTest.getMethod('mock')
        )
        node.commandInfo = new CommandInfo('', new PermissionInfo('permission', Permission.Default.NONE))

        and:
        def context = new CommandExecutionContext(
                new CommandSender(),
                (s, p) -> p.permissionDefault == Permission.Default.OP
        )

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.no-permission'
    }

    @SuppressWarnings('unused')
    static void mock() {

    }

}
