package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    private static final @NotNull Map<String, Queue<MockMessage>> MESSAGES = new ConcurrentHashMap<>();

    private final @NotNull List<Consumer<String>> consumers = new CopyOnWriteArrayList<>();

    private final @NotNull ScheduledExecutorService executorService;
    private long lastRead;

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
        this.executorService.scheduleAtFixedRate(
                () -> {
                    MockMessageQueryEngine.getQueue(getChannelName()).stream()
                            .filter(m -> m.timestamp() > lastRead)
                            .map(MockMessage::message)
                            .forEach(m -> consumers.forEach(c -> c.accept(m)));
                    lastRead = System.currentTimeMillis();
                },
                0,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public @NotNull CompletableFuture<Void> publish(final @NotNull String payload) {
        return CompletableFuture.runAsync(() ->
                MESSAGES.keySet().stream()
                        .filter(n -> !n.equals(getChannelName()))
                        .map(MESSAGES::get)
                        .forEach(q -> q.add(new MockMessage(payload, System.currentTimeMillis())))
        );
    }

    @Override
    public void listen(final @NotNull Consumer<String> consumer) {
        consumers.add(consumer);
    }

    @Override
    public void close() {
        MockMessageQueryEngine.MESSAGES.remove(getChannelName());
        executorService.shutdown();
    }

    /**
     * Clears all the messages.
     */
    static void clear() {
        MESSAGES.clear();
    }

    /**
     * Gets the queue with the given name.
     *
     * @param name the name
     * @return the queue
     */
    static @NotNull Queue<MockMessage> getQueue(final @NotNull String name) {
        return MESSAGES.computeIfAbsent(name, _ -> new ConcurrentLinkedQueue<>());
    }

    /**
     * A record representing a message.
     *
     * @param message   the message
     * @param timestamp the time when the message was received
     */
    record MockMessage(@NotNull String message, long timestamp) {
    }

}
