package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Mock {@link MessageBroker} for testing purposes.
 *
 * @see MockMessageChannelSettings
 */
public final class MockMessageBroker extends AbstractMessageBroker<MockMessageChannelSettings> {

    /**
     * Instantiates a new Mock message broker.
     *
     * @param mapper the mapper
     */
    MockMessageBroker(final @NotNull Mapper mapper) {
        super(mapper);
    }

    @Override
    public @NotNull MessageChannel newChannel(final @NonNull MockMessageChannelSettings settings) {
        throw new UnsupportedOperationException();
    }

}
