package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.argument.Time;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

/**
 * A manager for handling asynchronous executions of {@link CommandExecutor}s.
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
final class AsyncManager {
    private final @NotNull Set<Object> pending = ConcurrentHashMap.newKeySet();

    private final @NotNull ExecutorService executorService;
    private final @NotNull Duration timeout;

    /**
     * Executes the given {@link CommandExecutor} asynchronously.
     *
     * @param executor         the executor
     * @param executionVisitor the execution visitor
     * @return the future with the asynchronous execution
     * @throws CommandExecutionException in case of any error
     */
    public @NotNull CompletableFuture<Void> execute(final @NotNull CommandExecutor executor,
                                                    final @NotNull CommandExecutionVisitor executionVisitor) throws CommandExecutionException {
        CommandSenderWrapper<?> sender = executionVisitor.getCommandSender();
        Object id = sender.getId();
        if (pending.contains(id)) throw new CommandExecutionException("error.await-pending-operation");
        else pending.add(id);

        CompletableFuture<Void> checkTask = new CompletableFuture<>();
        Future<?> actualTask = executorService.submit(() -> {
            try {
                executor.execute(executionVisitor);
                checkTask.complete(null);
            } catch (CommandExecutionException e) {
                checkTask.completeExceptionally(e);
            }
        });

        if (!timeout.isZero()) checkTask.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

        return checkTask
                .handle((v, t) -> {
                    pending.remove(id);
                    actualTask.cancel(true);
                    if (t instanceof CompletionException) t = t.getCause();
                    else if (t instanceof TimeoutException)
                        t = new CommandExecutionException("error.operation-timeout")
                                .arguments(Time.of(timeout.toMillis()));
                    if (t != null) {
                        if (!(t instanceof CommandExecutionException))
                            t = new CommandExecutionException("error.internal-error", t)
                                    .arguments(Placeholder.of("message", t.getMessage()));
                        executionVisitor.handleCommandExecutionException((CommandExecutionException) t);
                    }
                    return v;
                });
    }

}
