package it.fulminazzo.blocksmith.broker.redis;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

public class RedisMessageChannel extends AbstractMessageChannel<RedisMessageQueryEngine> {
    public RedisMessageChannel(@NotNull RedisMessageQueryEngine queryEngine, @NotNull Mapper mapper) {
        super(queryEngine, mapper);
    }
}
