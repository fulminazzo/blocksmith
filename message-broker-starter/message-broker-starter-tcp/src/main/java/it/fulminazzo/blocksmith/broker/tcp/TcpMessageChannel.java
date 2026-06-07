package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for TCP connections.
 *
 * @see TcpMessageChannelSettings
 * @see TcpMessageQueryEngine
 */
public class TcpMessageChannel extends AbstractMessageChannel<TcpMessageQueryEngine> {

    /**
     * Instantiates a new Tcp message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected TcpMessageChannel(
            final @NotNull TcpMessageQueryEngine queryEngine,
            final @NotNull Mapper mapper
    ) {
        super(queryEngine, mapper);
    }

}
