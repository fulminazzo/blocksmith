package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Mock {@link MessageBroker} for testing purposes.
 *
 * @see MockMessageChannelSettings
 */
public final class MockMessageBroker extends AbstractMessageBroker<MockMessageChannelSettings> {

    @Override
    public @NotNull MessageChannel newChannel(final @NonNull MockMessageChannelSettings settings) {
        throw new UnsupportedOperationException();
    }

}
