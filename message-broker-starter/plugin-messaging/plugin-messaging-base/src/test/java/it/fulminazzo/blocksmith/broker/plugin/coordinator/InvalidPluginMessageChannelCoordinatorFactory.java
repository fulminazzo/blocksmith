package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageChannelCoordinatorFactory} that throws on initialization.
 *
 * @see MockPluginMessageChannelCoordinator
 */
public class InvalidPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    /**
     * Instantiates a new Invalid plugin message channel coordinator factory.
     */
    public InvalidPluginMessageChannelCoordinatorFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PluginMessageRegistrar createRegistrar(final @NotNull Object owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        throw new UnsupportedOperationException();
    }

}
