package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageChannelCoordinator}.
 *
 * @see MockPluginMessageRegistrar
 * @see MockPluginMessageChannelCoordinatorFactory
 */
final class MockPluginMessageChannelCoordinator extends PluginMessageChannelCoordinator {

    /**
     * Instantiates a new Mock plugin message channel coordinator.
     *
     * @param registrar the registrar
     */
    public MockPluginMessageChannelCoordinator(final @NotNull PluginMessageRegistrar registrar) {
        super(registrar);
    }

    @Override
    public boolean publish(final @NotNull String channelName, final byte @NotNull [] message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void republishFailedMessages() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void registerChannel(final @NotNull String channelName) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void unregisterChannel(final @NotNull String channelName) {
        throw new UnsupportedOperationException();
    }

}
