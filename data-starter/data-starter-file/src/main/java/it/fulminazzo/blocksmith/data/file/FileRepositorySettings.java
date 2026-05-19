package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;

/**
 * Repository settings for filesystem repositories.
 *
 * @see FileRepository
 * @see FileDataSource
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileRepositorySettings extends RepositorySettings {
    private @Nullable File dataDirectory;
    private @Nullable Logger logger;
    private @Nullable ConfigurationFormat format;

    /**
     * Sets the data directory.
     *
     * @param dataDirectory the data directory
     * @return this object (for method chaining)
     */
    public @NotNull FileRepositorySettings withDataDirectory(final @NotNull File dataDirectory) {
        this.dataDirectory = dataDirectory;
        return this;
    }

    /**
     * Sets the logger.
     *
     * @param logger the logger
     * @return this object (for method chaining)
     */
    public @NotNull FileRepositorySettings withLogger(final @NotNull Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Sets the configuration format to save the data with.
     *
     * @param format the format
     * @return this object (for method chaining)
     */
    public @NotNull FileRepositorySettings withFormat(final @NotNull ConfigurationFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Gets the data directory.
     *
     * @return the data directory
     */
    public @NotNull File getDataDirectory() {
        return Objects.requireNonNull(dataDirectory, "dataDirectory has not been specified yet");
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public @NotNull Logger getLogger() {
        return Objects.requireNonNull(logger, "logger has not been specified yet");
    }

    /**
     * Gets the configuration format to save the data with.
     *
     * @return the format
     */
    public @NotNull ConfigurationFormat getFormat() {
        return Objects.requireNonNull(format, "Configuration format has not been specified yet");
    }

}
