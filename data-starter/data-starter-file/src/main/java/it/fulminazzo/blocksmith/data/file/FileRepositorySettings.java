package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;

public final class FileRepositorySettings extends RepositorySettings {
    private @Nullable File dataDirectory;
    private @Nullable Logger logger;
    private @Nullable ConfigurationFormat format;

    public @NotNull File getDataDirectory() {
        return Objects.requireNonNull(dataDirectory, "dataDirectory has not been specified yet");
    }

    public @NotNull Logger getLogger() {
        return Objects.requireNonNull(logger, "logger has not been specified yet");
    }

    public @NotNull ConfigurationFormat getFormat() {
        return Objects.requireNonNull(format, "Configuration format has not been specified yet");
    }

    public @NotNull FileRepositorySettings withDataDirectory(final @NotNull File dataDirectory) {
        this.dataDirectory = dataDirectory;
        return this;
    }

    public @NotNull FileRepositorySettings withLogger(final @NotNull Logger logger) {
        this.logger = logger;
        return this;
    }

    public @NotNull FileRepositorySettings withFormat(final @NotNull ConfigurationFormat format) {
        this.format = format;
        return this;
    }
    
}
