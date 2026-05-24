package it.fulminazzo.blocksmith.broker.rabbitmq;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for RabbitMQ databases.
 *
 * @see RabbitMQMessageChannelSettings
 * @see RabbitMQMessageQueryEngine
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class RabbitMQMessageChannel extends AbstractMessageChannel<RabbitMQMessageQueryEngine> {

    /**
     * Instantiates a new RabbitMQ message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected RabbitMQMessageChannel(
            final @NotNull RabbitMQMessageQueryEngine queryEngine,
            final @NotNull Mapper mapper
    ) {
        super(queryEngine, mapper);
    }

}
