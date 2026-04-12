package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import it.fulminazzo.blocksmith.message.argument.Time;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.ToString;
//import org.jetbrains.annotations.NotNull;
//
//import java.time.Duration;
//import java.util.Set;
//import java.util.concurrent.*;
//
///**
// * A manager for handling asynchronous executions in {@link CommandNode}.
// */
//@EqualsAndHashCode
//@ToString
//@RequiredArgsConstructor
//final class AsyncManager {
//    private static final @NotNull ExecutorService executorService = Executors.newCachedThreadPool();
//
//    private final @NotNull Set<Object> pending = ConcurrentHashMap.newKeySet();
//    @Getter
//    private final @NotNull Duration timeout;
//
//    /**
//     * Executes the given {@link ExecutionInfo} asynchronously.
//     *
//     * @param executionInfo the execution info
//     * @param context       the context
//     * @return the future with the asynchronous execution
//     * @throws CommandExecutionException in case of any error
//     */
//    public @NotNull CompletableFuture<Void> execute(final @NotNull ExecutionInfo executionInfo,
//                                                    final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        CommandSenderWrapper<?> sender = context.getCommandSender();
//        Object id = sender.getId();
//        if (pending.contains(id)) throw new CommandExecutionException("error.await-pending-operation");
//        else pending.add(id);
//
//        CompletableFuture<Void> checkTask = new CompletableFuture<>();
//        Future<?> actualTask = executorService.submit(() -> {
//            try {
//                executionInfo.invoke(context);
//                checkTask.complete(null);
//            } catch (CommandExecutionException e) {
//                checkTask.completeExceptionally(e);
//            }
//        });
//
//        if (!timeout.isZero()) checkTask.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
//
//        return checkTask
//                .handle((v, t) -> {
//                    pending.remove(id);
//                    actualTask.cancel(true);
//                    if (t instanceof CompletionException) t = t.getCause();
//                    else if (t instanceof TimeoutException)
//                        t = new CommandExecutionException("error.operation-timeout")
//                                .arguments(Time.of(timeout.toMillis()));
//                    if (t != null) {
//                        if (!(t instanceof CommandExecutionException))
//                            t = new CommandExecutionException("error.internal-error", t)
//                                    .arguments(Placeholder.of("message", t.getMessage()));
//                        context.getRegistry().handleCommandExecutionException((CommandExecutionException) t, context);
//                    }
//                    return v;
//                });
//    }
//
//}
