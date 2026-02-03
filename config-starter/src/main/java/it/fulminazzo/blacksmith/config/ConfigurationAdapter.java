package it.fulminazzo.blacksmith.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A specialized adapter to load and store <b>Java beans</b>
 * representing configuration files in different language data formats.
 */
public interface ConfigurationAdapter {

    /**
     * Attempts to load the configuration file to the specified type.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>  the type of the configuration
     * @param file the file
     * @param type the class of the configuration
     * @return the loaded configuration
     */
    <T> @NotNull T load(final @NotNull File file,
                        final @NotNull Class<T> type);

    /**
     * Stores the given configuration to file.
     *
     * @param <T>           the type of the configuration
     * @param configuration the configuration
     * @param file          the file
     */
    <T> void store(final @NotNull T configuration,
                   final @NotNull File file);

}
