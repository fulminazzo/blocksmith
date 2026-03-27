//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandRegistry
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.time.Duration

class AsyncManagerTest extends Specification {
    private static final WAIT_TIME = 50

    private static boolean executed = false

    private AsyncManager manager

    private CommandRegistry registry
    private CommandSenderWrapper sender

    private CommandExecutionContext context

    void setup() {
        executed = false

        manager = new AsyncManager(Duration.ofSeconds(1))

        registry = Mock(CommandRegistry)

        sender = Mock(CommandSenderWrapper)
        sender.id >> UUID.randomUUID()

        context = new CommandExecutionContext(
                Mock(ApplicationHandle),
                registry,
                sender
        ).addInput('execute')
    }

    void cleanup() {
        executed = false
    }

    def 'test that execute does not throw'() {
        given:
        def executionInfo = getExecutionInfo('execute')

        when:
        def future = manager.execute(executionInfo, context)

        then:
        contains()

        when:
        manager.execute(executionInfo, context)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.await-pending-operation'

        when:
        future.join()

        then:
        noExceptionThrown()

        and:
        executed

        and:
        !contains()
    }

    def 'test that #method throws #expected'() {
        given:
        def executionInfo = getExecutionInfo(method)

        when:
        def future = manager.execute(executionInfo, context)

        then:
        contains()

        when:
        future.join()

        then:
        noExceptionThrown()

        and:
        1 * registry.handleCommandExecutionException(
                { CommandExecutionException e ->
                    e.message == expected
                },
                context
        )

        and:
        !contains()

        where:
        method                  || expected
        'internalThrow'         || 'error.unknown'
        'slow'                  || 'error.operation-timeout'
        'otherException'        || 'error.internal-error'
        'otherRuntimeException' || 'error.internal-error'
    }

    def 'test that execute re-throws CommandExecutionException on exception'() {
        given:
        def context = Mock(CommandExecutionContext)
        context.registry >> registry
        context.commandSender >> sender

        and:
        def executionInfo = getExecutionInfo('execute')

        when:
        manager.execute(executionInfo, context).join()

        then:
        noExceptionThrown()

        and:
        1 * registry.handleCommandExecutionException(
                { CommandExecutionException e ->
                    e.message == 'error.internal-error'
                },
                context
        )

        and:
        !contains()
    }

    private ExecutionInfo getExecutionInfo(final @NotNull String method) {
        return new ExecutionInfo(
                AsyncManagerTest,
                AsyncManagerTest.getMethod(method)
        )
    }

    private boolean contains() {
        return manager.pending.contains(sender.id)
    }

    static void execute() {
        sleep(WAIT_TIME)
        executed = true
    }

    static void internalThrow() {
        sleep(WAIT_TIME)
        throw new CommandExecutionException('error.unknown')
    }

    static void slow() {
        sleep(10_000)
    }

    static void otherException() {
        sleep(WAIT_TIME)
        throw new Exception('API error')
    }

    static void otherRuntimeException() {
        sleep(WAIT_TIME)
        throw new RuntimeException('API error')
    }

}
