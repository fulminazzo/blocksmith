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
@With
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

}
