package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

/**
 * Mock {@link MessageChannelSettings} for testing purposes.
 *
 * @see MockMessageBroker
 */
public final class MockMessageChannelSettings extends MessageChannelSettings {

    @Override
    public @NotNull MockMessageChannelSettings withChannelName(final @NotNull String channelName) {
        return (MockMessageChannelSettings) super.withChannelName(channelName);
    }

    @Override
    public @NotNull MockMessageChannelSettings broadcast() {
        return (MockMessageChannelSettings) super.broadcast();
    }

    @Override
    public @NotNull MockMessageChannelSettings direct(final @NotNull String subchannelName) {
        return (MockMessageChannelSettings) super.direct(subchannelName);
    }

}
