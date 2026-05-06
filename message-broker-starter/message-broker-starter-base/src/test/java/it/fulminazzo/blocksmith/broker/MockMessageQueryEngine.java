package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class MockMessageQueryEngine extends MessageQueryEngine {
    static final @NotNull Map<String, Queue<String>> MESSAGES = new ConcurrentHashMap<>();

    private final @NotNull ScheduledExecutorService executorService;

    public MockMessageQueryEngine(final @NotNull String channelName,
                                  final @NotNull ScheduledExecutorService executorService) {
        super(channelName);
        this.executorService = executorService;
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() ->
                MESSAGES.keySet().stream()
                        .filter(n -> !n.equals(channelName))
                        .map(MESSAGES::get)
                        .forEach(q -> q.add(payload))
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        executorService.scheduleAtFixedRate(
                () -> {
                    Queue<String> queue = MockMessageQueryEngine.getQueue(channelName);
                    if (!queue.isEmpty()) consumer.accept(queue.poll());
                },
                0,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() {
        MockMessageQueryEngine.MESSAGES.remove(channelName);
    }

    public static @NotNull Queue<String> getQueue(final @NotNull String name) {
        return MESSAGES.computeIfAbsent(name, _ -> new ConcurrentLinkedQueue<>());
    }

}
