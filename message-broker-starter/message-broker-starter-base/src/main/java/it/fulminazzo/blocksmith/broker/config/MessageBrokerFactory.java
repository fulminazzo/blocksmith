package it.fulminazzo.blocksmith.broker.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import org.jetbrains.annotations.NotNull;

/**
 * A factory to create a new {@link MessageBroker} from a configuration.
 *
 * @see MessageBrokerConfig
 * @see MessageBroker
 */
@FunctionalInterface
public interface MessageBrokerFactory {

    /**
     * Builds the Message broker.
     *
     * @param config the config
     * @return the message broker
     */
    @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig config);

}
