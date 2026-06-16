package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageRegistrar}.
 *
 * @see MockPluginMessageChannelCoordinator
 */
public final class MockPluginMessageRegistrar implements PluginMessageRegistrar {

    @Override
    public @NotNull <P> P plugin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <S> S server() {
        throw new UnsupportedOperationException();
    }

}
