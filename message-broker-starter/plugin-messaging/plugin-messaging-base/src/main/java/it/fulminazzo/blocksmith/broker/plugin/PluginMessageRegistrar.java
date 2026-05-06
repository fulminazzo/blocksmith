package it.fulminazzo.blocksmith.broker.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Identifies a class that can register plugin messages.
 */
public interface PluginMessageRegistrar {

    /**
     * Gets the server that the application is running on.
     *
     * @param <S> the type of the server
     * @return the server
     */
    <S> @NotNull S server();

    /**
     * Gets the name of the application in lower case.
     *
     * @return the name
     */
    default @NotNull String lowercaseName() {
        return getName().toLowerCase();
    }

    /**
     * Gets the name of the application.
     *
     * @return the name
     */
    @NotNull String getName();

}
