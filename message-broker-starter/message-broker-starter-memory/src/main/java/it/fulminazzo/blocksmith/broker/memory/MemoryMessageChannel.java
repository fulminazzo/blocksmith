package it.fulminazzo.blocksmith.broker.memory;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperFormat;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for in-memory databases.
 */
public class MemoryMessageChannel extends AbstractMessageChannel<MemoryMessageQueryEngine> {

    /**
     * Instantiates a new Memory message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected MemoryMessageChannel(final @NotNull MemoryMessageQueryEngine queryEngine,
                                   final @NotNull Mapper mapper) {
        super(queryEngine, mapper);
    }

    /**
     * Creates a new Memory message channel.
     * <br>
     * Uses a {@link MapperFormat#SERIALIZABLE} mapper under the hood.
     *
     * @param channelName the channel name
     * @return the memory message channel
     */
    public static @NotNull MemoryMessageChannel create(final @NotNull String channelName) {
        return new MemoryMessageChannel(
                new MemoryMessageQueryEngine(channelName, Executors.newSingleThreadExecutor()),
                MapperFormat.SERIALIZABLE.newMapper()
        );
    }

}
