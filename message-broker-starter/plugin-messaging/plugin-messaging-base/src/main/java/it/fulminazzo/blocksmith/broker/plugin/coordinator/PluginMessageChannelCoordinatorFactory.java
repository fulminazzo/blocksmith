package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * A factory for creating {@link PluginMessageRegistrar} and {@link PluginMessageChannelCoordinator} instances.
 *
 * @see PluginMessageRegistrar
 * @see PluginMessageChannelCoordinator
 */
public interface PluginMessageChannelCoordinatorFactory {

    /**
     * Creates a new {@link PluginMessageChannelCoordinator} instance.
     *
     * @param registrar the registrar
     * @return the coordinator
     */
    @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar);

    /**
     * Creates a new {@link PluginMessageChannelCoordinator} instance.
     *
     * @param owner the owner of the coordinator
     * @return the coordinator
     */
    default @NotNull PluginMessageChannelCoordinator create(final @NotNull Object owner) {
        return create(createRegistrar(owner));
    }

    /**
     * Creates a new {@link PluginMessageRegistrar} instance.
     *
     * @param owner the owner of the registrar
     * @return the registrar
     */
    @NotNull PluginMessageRegistrar createRegistrar(final @NotNull Object owner);

    /**
     * Checks if the current factory supports the provided owner type.
     *
     * @param ownerType the owner type
     * @return {@code true} if it does
     */
    boolean supportsOwner(final @NotNull Class<?> ownerType);

}
