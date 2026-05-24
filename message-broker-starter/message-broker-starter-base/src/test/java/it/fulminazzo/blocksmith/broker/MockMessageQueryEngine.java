package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Mock implementation of {@link MessageQueryEngine} for {@link MockMessageChannel}.
 *
 * @see MockMessageChannel
 */
public final class MockMessageQueryEngine extends MessageQueryEngine {
    /**
     * The Messages.
     */
    static final @NotNull Map<String, Queue<String>> MESSAGES = new ConcurrentHashMap<>();

    private final @NotNull ScheduledExecutorService executorService;

    /**
     * Instantiates a new Mock message query engine.
     *
     * @param channelName     the channel name
     * @param executorService the executor service
     */
    public MockMessageQueryEngine(
            final @NotNull String channelName,
            final @NotNull ScheduledExecutorService executorService
    ) {
        super(channelName);
        this.executorService = executorService;
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() ->
                MESSAGES.keySet().stream()
                        .filter(n -> !n.equals(getChannelName()))
                        .map(MESSAGES::get)
                        .forEach(q -> q.add(payload))
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        executorService.scheduleAtFixedRate(
                () -> {
                    Queue<String> queue = MockMessageQueryEngine.getQueue(getChannelName());
                    if (!queue.isEmpty()) consumer.accept(queue.poll());
                },
                0,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() {
        MockMessageQueryEngine.MESSAGES.remove(getChannelName());
    }

    /**
     * Gets the queue with the given name.
     *
     * @param name the name
     * @return the queue
     */
    public static @NotNull Queue<String> getQueue(final @NotNull String name) {
        return MESSAGES.computeIfAbsent(name, _ -> new ConcurrentLinkedQueue<>());
    }

}
