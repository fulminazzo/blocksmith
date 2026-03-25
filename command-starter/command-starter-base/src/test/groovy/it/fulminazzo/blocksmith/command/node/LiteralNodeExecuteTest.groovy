package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
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
        def context = new CommandExecutionContext(Mock(ApplicationHandle), new MockCommandSenderWrapper(new CommandSender().setOp(true)))

        when:
        node.execute(context)

        then:
        noExceptionThrown()

        where:
        commandInfo << [
                null,
                new CommandInfo('', new PermissionInfo('permission', Permission.Grant.ALL)),
                new CommandInfo('', new PermissionInfo('permission', Permission.Grant.OP))
        ]
    }

    def 'test that validateInput throws for no permission'() {
        given:
        def node = new LiteralNode('test')
        node.executionInfo = new ExecutionInfo(
                LiteralNodeExecuteTest,
                LiteralNodeExecuteTest.getMethod('mock')
        )
        node.commandInfo = new CommandInfo('', new PermissionInfo('permission', Permission.Grant.NONE))

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), new MockCommandSenderWrapper(new CommandSender().setOp(true)))

        when:
        node.execute(context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.no-permission'
    }

    def 'test that getCompletions returns #expected'() {
        given:
        def node = new LiteralNode('first', 'second', 'third')
        node.commandInfo = new CommandInfo('', new PermissionInfo('permission', Permission.Grant.OP))

        and:
        def context = new CommandExecutionContext(Mock(ApplicationHandle), new MockCommandSenderWrapper(new CommandSender().setOp(op)))

        when:
        def actual = node.getCompletions(context)

        then:
        actual.sort() == expected.sort()

        where:
        op    || expected
        false || []
        true  || ['first', 'second', 'third']
    }

    @SuppressWarnings('unused')
    static void mock() {

    }

}
