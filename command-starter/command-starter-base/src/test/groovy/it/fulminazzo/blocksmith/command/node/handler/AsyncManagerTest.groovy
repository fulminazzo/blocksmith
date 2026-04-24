//file:noinspection unused
package it.fulminazzo.blocksmith.command.node.handler

import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AsyncManagerTest extends Specification {
    private static final waitTime = 50
    private static final ExecutorService executorService = Executors.newCachedThreadPool()

    private static volatile boolean executed = false

    private AsyncManager manager

    private CommandSenderWrapper sender

    private CommandExecutionVisitor visitor

    void setup() {
        executed = false

        manager = new AsyncManager(executorService, Duration.ofSeconds(1))

        sender = Mock(CommandSenderWrapper)
        sender.idImpl >> UUID.randomUUID()
        sender.sync { _ } >> { a ->
            a[0].accept(sender)
        }

        visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.arguments >> []
    }

    void cleanup() {
        executed = false
    }

    void cleanupSpec() {
        executorService.shutdown()
    }

    def 'test that execute does not throw'() {
        given:
        def executor = getExecutor('execute')

        when:
        def future = manager.execute(executor, visitor)

        then:
        contains()

        when:
        manager.execute(executor, visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == 'error.await-pending-operation'

        when:
        future.join()

        then:
        0 * visitor.handleCommandExecutionException(_)

        and:
        executed

        and:
        !contains()
    }

    def 'test that #method throws #expected'() {
        given:
        def executor = getExecutor(method)

        when:
        def future = manager.execute(executor, visitor)

        then:
        contains()

        when:
        future.join()

        and:
        sleep(100)

        then:
        noExceptionThrown()

        and:
        1 * visitor.handleCommandExecutionException(_) >> { a ->
            assert a[0].message == expected
        }

        and:
        !contains()

        and:
        !executed

        where:
        method                  || expected
        'internalThrow'         || 'error.unknown'
        'slow'                  || 'error.operation-timeout'
        'otherException'        || 'error.internal-error'
        'otherRuntimeException' || 'error.internal-error'
    }

    def 'test that execute re-throws CommandExecutionException on exception'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender

        and:
        def executor = getExecutor('execute')

        when:
        manager.execute(executor, visitor).join()

        then:
        noExceptionThrown()

        and:
        1 * visitor.handleCommandExecutionException(_) >> { a ->
            assert a[0].message == 'error.internal-error'
        }

        and:
        !contains()
    }

    def 'test that execute re-throws CommandExecutionException on CompletionException'() {
        given:
        def executor = Mock(CommandExecutor)
        executor.execute(_) >> {
            throw new CompletionException(new Exception('Unknown error'))
        }

        when:
        manager.execute(executor, visitor).join()

        then:
        noExceptionThrown()

        and:
        1 * visitor.handleCommandExecutionException(_) >> { a ->
            assert a[0].message == 'error.internal-error'
            def cause = a[0].cause
            assert cause != null
            assert cause.message == 'Unknown error'
        }

        and:
        !contains()
    }

    private boolean contains() {
        return manager.pending.contains(sender.id)
    }

    private CommandExecutor getExecutor(final @NotNull String method) {
        return new CommandExecutor(
                AsyncManagerTest,
                AsyncManagerTest.getMethod(method)
        )
    }

    static void execute() {
        Thread.sleep(waitTime)
        executed = true
    }

    static void internalThrow() {
        Thread.sleep(waitTime)
        throw new CommandExecutionException('error.unknown')
    }

    static void slow() {
        Thread.sleep(1050)
        executed = true
    }

    static void otherException() {
        Thread.sleep(waitTime)
        throw new Exception('API error')
    }

    static void otherRuntimeException() {
        Thread.sleep(waitTime)
        throw new RuntimeException('API error')
    }

}
