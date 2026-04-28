package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Test message channel with handling of messages through internal static map.
 */
public final class MockMessageChannel extends AbstractMessageChannel {
    private static final @NotNull Map<String, Queue<String>> MESSAGES = new ConcurrentHashMap<>();

    @Getter
    private final @NotNull String name;

    /**
     * Instantiates a new Mock message channel.
     * Uses the executor to <b>receive</b> messages.
     *
     * @param mapper          the mapper
     * @param name            the name
     * @param executorService the executor service
     */
    public MockMessageChannel(final @NotNull Mapper mapper,
                              final @NotNull String name,
                              final @NotNull ScheduledExecutorService executorService) {
        super(mapper);
        this.name = name;
        executorService.scheduleAtFixedRate(
                () -> {
                    Queue<String> queue = getQueue(name);
                    if (!queue.isEmpty()) handleMessage(queue.poll());
                },
                0,
                125,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected @NotNull CompletableFuture<Void> sendRawImpl(final @NotNull String payload) {
        return CompletableFuture.runAsync(() ->
                MESSAGES.keySet().stream()
                        .filter(n -> !n.equals(name))
                        .map(MESSAGES::get)
                        .forEach(q -> q.add(payload))
        );
    }

    public static @NotNull Queue<String> getQueue(final @NotNull String name) {
        return MESSAGES.computeIfAbsent(name, _ -> new ConcurrentLinkedQueue<>());
    }

    @Override
    public void close() {
        MESSAGES.remove(name);
    }

}
