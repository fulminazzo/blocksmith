package it.fulminazzo.blocksmith.broker.plugin.coordinator;

import org.jetbrains.annotations.NotNull;

/**
 * An object responsible for registering and unregistering plugin messaging channels.
 *
 * @see PluginMessageChannelCoordinator
 */
public interface PluginMessageRegistrar {

    /**
     * Gets the plugin to register the channel for.
     *
     * @param <P> the type of the plugin
     * @return the plugin
     */
    <P> @NotNull P plugin();

    /**
     * Gets the server the application is running on.
     *
     * @param <S> the type of the server
     * @return the server
     */
    <S> @NotNull S server();

}
