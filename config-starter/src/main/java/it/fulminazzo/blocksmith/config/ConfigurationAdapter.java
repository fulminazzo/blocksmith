package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A specialized adapter to load and store <b>Java beans</b>
 * representing configuration files in different language data formats.
 * <br>
 * Supports interchangeability of data format languages.
 */
public interface ConfigurationAdapter extends BaseConfigurationAdapter {

    /**
     * Attempts to load the configuration file to the specified type.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>             the type of the configuration
     * @param parentDirectory the directory where the file is contained
     * @param fileName        the name of the file (without the extension.
     *                        The current {@link ConfigurationFormat} will be used to determine it)
     * @param type            the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T load(final @NotNull File parentDirectory,
                        final @NotNull String fileName,
                        final @NotNull Class<T> type) throws IOException;

    /**
     * Stores the given configuration to file.
     *
     * @param <T>             the type of the configuration
     * @param parentDirectory the directory where the file is contained
     * @param fileName        the name of the file (without the extension.
     *                        The current {@link ConfigurationFormat} will be used to determine it)
     * @param configuration   the configuration
     * @throws IOException in case of any errors
     */
    <T> void store(final @NotNull File parentDirectory,
                   final @NotNull String fileName,
                   final @NotNull T configuration) throws IOException;

    /**
     * Updates the format for this configuration adapter.
     *
     * @param format the format
     * @return this configuration adapter
     */
    @NotNull ConfigurationAdapter setFormat(final @NotNull ConfigurationFormat format);

    /**
     * Instantiates a new Configuration adapter.
     *
     * @param logger the logger
     * @param format the format
     * @return the configuration adapter
     */
    static @NotNull ConfigurationAdapter newAdapter(final @NotNull Logger logger,
                                                    final @NotNull ConfigurationFormat format) {
        return new DelegateConfigurationAdapter(logger).setFormat(format);
    }

}
