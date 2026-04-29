package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Test message channel with handling of messages through internal static map.
 */
public final class MockMessageChannel extends AbstractMessageChannel<MockMessageQueryEngine> {
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
        super(new MockMessageQueryEngine(name, executorService), mapper);
        this.name = name;
    }

}
