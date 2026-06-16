package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageChannelCoordinatorFactory}.
 *
 * @see MockPluginMessageChannelCoordinator
 */
public final class MockPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        return new MockPluginMessageChannelCoordinator(registrar);
    }

    @Override
    public @NotNull PluginMessageRegistrar createRegistrar(final @NotNull Object owner) {
        return new MockPluginMessageRegistrar();
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return true;
    }

}
