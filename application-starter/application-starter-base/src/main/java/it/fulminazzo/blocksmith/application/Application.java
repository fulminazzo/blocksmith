package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

/**
 * Main entry point of a Blocksmith application.
 *
 * @see BlocksmithApplication
 */
public interface Application {

    /**
     * Function executed when the application starts.
     */
    void onStartup();

    /**
     * Function executed when the application stops.
     */
    void onShutdown();

    /**
     * Gets the name of the application.
     *
     * @return the name of the application
     */
    @NotNull String getName();

    /**
     * Gets the version of the application.
     *
     * @return the version of the application
     */
    @NotNull String getVersion();

    /**
     * Gets the folder where the application can operate.
     *
     * @return the folder
     */
    @NotNull File getFolder();

    /**
     * Gets the logger of the application.
     *
     * @return the logger
     */
    @NotNull Logger getLogger();

    /**
     * Gets the platform where the application is running.
     * This may vary depending on the type of application and platform.
     * Therefore, the generic return type should be used with caution.
     *
     * @param <H> the type of the platform
     * @return the platform
     */
    <H> @NotNull H getHost();

}
