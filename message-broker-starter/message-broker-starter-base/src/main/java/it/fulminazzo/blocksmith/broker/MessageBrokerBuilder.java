package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general builder for a {@link MessageBroker}.
 *
 * @param <B> the type of the message broker
 */
public interface MessageBrokerBuilder<B extends MessageBroker<?>> {

    /**
     * Builds the message broker.
     *
     * @return the message broker
     */
    @NotNull B build();

}
