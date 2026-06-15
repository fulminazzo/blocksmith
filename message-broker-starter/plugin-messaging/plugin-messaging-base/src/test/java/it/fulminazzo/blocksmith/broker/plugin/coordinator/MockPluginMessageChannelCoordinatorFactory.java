package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageChannelCoordinatorFactory}.
 *
 * @see MockPluginMessageChannelCoordinator
 */
public class MockPluginMessageChannelCoordinatorFactory implements PluginMessageChannelCoordinatorFactory {

    @Override
    public @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar) {
        return new MockPluginMessageChannelCoordinator(registrar);
    }

    @Override
    public @NotNull PluginMessageRegistrar createRegistrar(final @NotNull Object owner) {
        return new MockPluginMessageRegistrar((MockOwner) owner);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return MockOwner.class.isAssignableFrom(ownerType);
    }

}
