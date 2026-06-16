package it.fulminazzo.blocksmith.broker.memory.config;

import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories;
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageBroker;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * {@link MessageBrokerConfig} for {@link MemoryMessageBroker}.
 *
 * @see MessageBrokerConfig
 * @see MemoryMessageBroker
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class MemoryMessageBrokerConfig extends MessageBrokerConfig<MemoryMessageBrokerConfig> {

    static {
        MessageBrokerFactories.registerFactory(
                MemoryMessageBrokerConfig.class,
                config -> MemoryMessageBroker.create(config.getMapper())
        );
    }

}

