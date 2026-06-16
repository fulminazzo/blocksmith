package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of {@link PluginMessageRegistrar} for testing purposes only.
 *
 * @see MockPluginMessageChannelCoordinator
 */
final class MockPluginMessageRegistrar implements PluginMessageRegistrar {
    private final @NotNull MockOwner owner;

    /**
     * Instantiates a new Mock plugin message registrar.
     *
     * @param owner the owner
     */
    public MockPluginMessageRegistrar(final @NotNull MockOwner owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <P> P plugin() {
        return (P) owner;
    }

    @Override
    public @NotNull <S> S server() {
        throw new UnsupportedOperationException();
    }

}
