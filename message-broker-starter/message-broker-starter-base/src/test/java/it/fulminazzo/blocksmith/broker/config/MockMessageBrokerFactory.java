package it.fulminazzo.blocksmith.broker.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.broker.MockMessageBroker;
import it.fulminazzo.blocksmith.data.mapper.MapperFormat;
import org.jetbrains.annotations.NotNull;

/**
 * Mock {@link MessageBrokerFactory} for testing purposes.
 *
 * @see MockMessageBrokerConfig
 */
public final class MockMessageBrokerFactory implements MessageBrokerFactory {

    @Override
    public @NotNull MessageBroker<?> build(final @NotNull MessageBrokerConfig config) {
        return new MockMessageBroker(MapperFormat.JSON.newMapper());
    }

}
