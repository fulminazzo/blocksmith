package it.fulminazzo.blocksmith.broker.redis;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for Redis databases.
 */
public class RedisMessageChannel extends AbstractMessageChannel<RedisMessageQueryEngine> {

    /**
     * Instantiates a new Redis message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected RedisMessageChannel(final @NotNull RedisMessageQueryEngine queryEngine,
                                  final @NotNull Mapper mapper) {
        super(queryEngine, mapper);
    }

}
