package it.fulminazzo.blocksmith.broker;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public final class MockMessageQueryEngine implements MessageQueryEngine {
    static final @NotNull Map<String, Queue<String>> MESSAGES = new ConcurrentHashMap<>();

    private final @NotNull String name;
    private final @NotNull ScheduledExecutorService executorService;

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() ->
                MESSAGES.keySet().stream()
                        .filter(n -> !n.equals(name))
                        .map(MESSAGES::get)
                        .forEach(q -> q.add(payload))
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        executorService.scheduleAtFixedRate(
                () -> {
                    Queue<String> queue = MockMessageQueryEngine.getQueue(name);
                    if (!queue.isEmpty()) consumer.accept(queue.poll());
                },
                0,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() {
        MockMessageQueryEngine.MESSAGES.remove(name);
    }

    public static @NotNull Queue<String> getQueue(final @NotNull String name) {
        return MESSAGES.computeIfAbsent(name, _ -> new ConcurrentLinkedQueue<>());
    }

}
