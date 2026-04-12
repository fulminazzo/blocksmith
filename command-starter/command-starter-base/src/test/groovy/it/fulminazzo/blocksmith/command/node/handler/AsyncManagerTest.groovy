//TODO: update
////file:noinspection unused
//package it.fulminazzo.blocksmith.command.node.handler
//
//import it.fulminazzo.blocksmith.ApplicationHandle
//import it.fulminazzo.blocksmith.command.CommandRegistry
//import it.fulminazzo.blocksmith.command.CommandSenderWrapper
//import it.fulminazzo.blocksmith.command.visitor.execution.ExecutionContext
//import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
//import org.jetbrains.annotations.NotNull
//import spock.lang.Specification
//
//import java.time.Duration
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class AsyncManagerTest extends Specification {
//    private static final waitTime = 50
//    private static final ExecutorService executorService = Executors.newCachedThreadPool()
//
//    private static volatile boolean executed = false
//
//    private AsyncManager manager
//
//    private CommandRegistry registry
//    private CommandSenderWrapper sender
//
//    private ExecutionContext context
//
//    void setup() {
//        executed = false
//
//        manager = new AsyncManager(executorService, Duration.ofSeconds(1))
//
//        registry = Mock(CommandRegistry)
//
//        sender = Mock(CommandSenderWrapper)
//        sender.id >> UUID.randomUUID()
//
//        context = new ExecutionContext(
//                Mock(ApplicationHandle),
//                registry,
//                sender
//        ).addInput('execute')
//    }
//
//    void cleanup() {
//        executed = false
//    }
//
//    void cleanupSpec() {
//        executorService.shutdown()
//    }
//
//    def 'test that execute does not throw'() {
//        given:
//        def executionInfo = getExecutor('execute')
//
//        when:
//        def future = manager.execute(executionInfo, context)
//
//        then:
//        contains()
//
//        when:
//        manager.execute(executionInfo, context)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        e.message == 'error.await-pending-operation'
//
//        when:
//        future.join()
//
//        then:
//        noExceptionThrown()
//
//        and:
//        executed
//
//        and:
//        !contains()
//    }
//
//    def 'test that #method throws #expected'() {
//        given:
//        def executionInfo = getExecutor(method)
//
//        when:
//        def future = manager.execute(executionInfo, context)
//
//        then:
//        contains()
//
//        when:
//        future.join()
//
//        and:
//        sleep(100)
//
//        then:
//        noExceptionThrown()
//
//        and:
//        1 * registry.handleCommandExecutionException(
//                { CommandExecutionException e ->
//                    e.message == expected
//                },
//                context
//        )
//
//        and:
//        !contains()
//
//        and:
//        !executed
//
//        where:
//        method                  || expected
//        'internalThrow'         || 'error.unknown'
//        'slow'                  || 'error.operation-timeout'
//        'otherException'        || 'error.internal-error'
//        'otherRuntimeException' || 'error.internal-error'
//    }
//
//    def 'test that execute re-throws CommandExecutionException on exception'() {
//        given:
//        def context = Mock(ExecutionContext)
//        context.registry >> registry
//        context.commandSender >> sender
//
//        and:
//        def executionInfo = getExecutor('execute')
//
//        when:
//        manager.execute(executionInfo, context).join()
//
//        then:
//        noExceptionThrown()
//
//        and:
//        1 * registry.handleCommandExecutionException(
//                { CommandExecutionException e ->
//                    e.message == 'error.internal-error'
//                },
//                context
//        )
//
//        and:
//        !contains()
//    }
//
//    private boolean contains() {
//        return manager.pending.contains(sender.id)
//    }
//
//    private static CommandExecutor getExecutor(final @NotNull String method) {
//        return new CommandExecutor(
//                AsyncManagerTest,
//                AsyncManagerTest.getMethod(method)
//        )
//    }
//
//    static void execute() {
//        Thread.sleep(waitTime)
//        executed = true
//    }
//
//    static void internalThrow() {
//        Thread.sleep(waitTime)
//        throw new CommandExecutionException('error.unknown')
//    }
//
//    static void slow() {
//        Thread.sleep(1050)
//        executed = true
//    }
//
//    static void otherException() {
//        Thread.sleep(waitTime)
//        throw new Exception('API error')
//    }
//
//    static void otherRuntimeException() {
//        Thread.sleep(waitTime)
//        throw new RuntimeException('API error')
//    }
//
//}
