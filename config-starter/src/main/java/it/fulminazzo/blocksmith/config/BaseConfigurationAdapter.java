package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A specialized adapter to load and store <b>Java beans</b>
 * representing configuration files in different language data formats.
 */
public interface BaseConfigurationAdapter {

    /**
     * Attempts to load the configuration to the specified type.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>  the type of the configuration
     * @param data the raw data
     * @param type the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T load(final @NotNull String data, final @NotNull Class<T> type) throws IOException;

    /**
     * Attempts to load the configuration file to the specified type.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>  the type of the configuration
     * @param file the file
     * @param type the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T load(final @NotNull File file, final @NotNull Class<T> type) throws IOException;

    /**
     * Attempts to load the configuration to the specified type.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>    the type of the configuration
     * @param stream the stream of data to load from
     * @param type   the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T load(final @NotNull InputStream stream, final @NotNull Class<T> type) throws IOException;

    /**
     * Attempts to load the configuration to the specified type from a resource of the current classloader.
     * <br>
     * <b>WARNING:</b> requires the class to have a <b>no arguments constructor</b>.
     *
     * @param <T>      the type of the configuration
     * @param resource the resource name
     * @param type     the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T loadFromResource(final @NotNull String resource, final @NotNull Class<T> type) throws IOException;

    /**
     * Serializes the given configuration to the format of this adapter.
     *
     * @param <T>           the type of the configuration
     * @param configuration the configuration
     * @return the @ not null string
     * @throws IOException in case of any errors
     */
    <T> @NotNull String serialize(final @NotNull T configuration) throws IOException;

    /**
     * Stores the given configuration to file.
     *
     * @param <T>           the type of the configuration
     * @param file          the file
     * @param configuration the configuration
     * @throws IOException in case of any errors
     */
    <T> void store(final @NotNull File file, final @NotNull T configuration) throws IOException;

    /**
     * Stores the given configuration to a general output stream.
     *
     * @param <T>           the type of the configuration
     * @param stream        the stream of data to store to
     * @param configuration the configuration
     * @throws IOException in case of any errors
     */
    <T> void store(final @NotNull OutputStream stream, final @NotNull T configuration) throws IOException;

    /**
     * Tries to find the specified resource in the specified directory.
     * If it was found, it will be loaded.
     * If it was not found, will lookup the current classloader for the resource
     * and extract it to the specified directory before loading it.
     *
     * @param <T>       the type of the configuration
     * @param resource  the resource name
     * @param directory the directory to look for the configuration
     * @param type      the class of the configuration
     * @return the loaded configuration
     * @throws IOException in case of any errors
     */
    <T> @NotNull T extractAndLoad(final @NotNull String resource,
                                  final @NotNull File directory,
                                  final @NotNull Class<T> type) throws IOException;

}
