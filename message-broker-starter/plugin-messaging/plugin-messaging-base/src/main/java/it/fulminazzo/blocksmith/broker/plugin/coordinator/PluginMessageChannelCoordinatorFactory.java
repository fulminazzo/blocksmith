package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * A factory for creating {@link PluginMessageChannelCoordinator} instances.
 *
 * @see PluginMessageChannelCoordinator
 */
public interface PluginMessageChannelCoordinatorFactory {

    /**
     * Creates a new {@link PluginMessageChannelCoordinator} instance.
     *
     * @param registrar the registrar to use for the coordinator
     * @return the coordinator
     */
    @NotNull PluginMessageChannelCoordinator create(final @NotNull PluginMessageRegistrar registrar);

    /**
     * Checks if the factory supports the given registrar.
     *
     * @param registrar the registrar
     * @return {@code true} if it does
     */
    boolean supportsRegistrar(final @NotNull PluginMessageRegistrar registrar);

}
