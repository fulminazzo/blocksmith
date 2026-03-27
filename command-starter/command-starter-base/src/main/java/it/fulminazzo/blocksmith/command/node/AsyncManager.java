package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.argument.Time;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

/**
 * A manager for handling asynchronous executions in {@link CommandNode}.
 */
@RequiredArgsConstructor
final class AsyncManager {
    private final @NotNull Set<Object> pending = ConcurrentHashMap.newKeySet();
    @Getter
    private final @NotNull Duration timeout;

    /**
     * Executes the given {@link ExecutionInfo} asynchronously.
     *
     * @param executionInfo the execution info
     * @param context       the context
     * @return the future with the asynchronous execution
     * @throws CommandExecutionException in case of any error
     */
    public synchronized @NotNull CompletableFuture<Void> execute(final @NotNull ExecutionInfo executionInfo,
                                                                 final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        CommandSenderWrapper sender = context.getCommandSender();
        Object id = sender.getId();
        if (pending.contains(id)) throw new CommandExecutionException("error.await-pending-operation");
        else {
            pending.add(id);
            return CompletableFuture.runAsync(() -> {
                        try {
                            executionInfo.invoke(context);
                        } catch (CommandExecutionException e) {
                            throw new CompletionException(e);
                        }
                    }).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .handle((v, t) -> {
                        pending.remove(id);
                        if (t instanceof CompletionException) t = t.getCause();
                        else if (t instanceof TimeoutException)
                            t = new CommandExecutionException("error.operation-timeout")
                                    .arguments(Time.of(timeout.toMillis()));
                        if (t != null) {
                            if (!(t instanceof CommandExecutionException))
                                t = new CommandExecutionException("error.internal-error", t)
                                        .arguments(Placeholder.of("message", t.getMessage()));
                            context.getRegistry().handleCommandExecutionException((CommandExecutionException) t, context);
                        }
                        return v;
                    });
        }
    }

}
