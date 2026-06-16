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
        return new MockPluginMessageChannelCoordinator();
    }

    @Override
    public @NotNull PluginMessageRegistrar createRegistrar(@NotNull Object owner) {
        return new PluginMessageRegistrar() {

            @Override
            public @NotNull <P> P plugin() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull <S> S server() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return true;
    }

}
