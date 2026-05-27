package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.broker.AbstractMessageChannel;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link it.fulminazzo.blocksmith.broker.MessageChannel} for Kafka databases.
 *
 * @see KafkaMessageChannelSettings
 * @see KafkaMessageQueryEngine
 */
public class KafkaMessageChannel extends AbstractMessageChannel<KafkaMessageQueryEngine> {

    /**
     * Instantiates a new Kafka message channel.
     *
     * @param queryEngine the query engine
     * @param mapper      the mapper
     */
    protected KafkaMessageChannel(
            final @NotNull KafkaMessageQueryEngine queryEngine,
            final @NotNull Mapper mapper
    ) {
        super(queryEngine, mapper);
    }

}
