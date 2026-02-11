package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * A general File repository builder.
 *
 * @param <B> the type of the builder
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor
@AllArgsConstructor
abstract class AbstractFileRepositoryBuilder<B extends AbstractFileRepositoryBuilder<B>> {
    protected @Nullable File dataDirectory;

    protected @Nullable Executor executor;

    protected @Nullable ConfigurationFormat format;

    protected @Nullable Logger logger;

    /**
     * Sets the directory where data will be stored.
     *
     * @param dataDirectory the data directory
     * @return this object (for method chaining)
     */
    public @NotNull B dataDirectory(final @NotNull File dataDirectory) {
        this.dataDirectory = dataDirectory;
        return (B) this;
    }

    /**
     * Sets executor.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull B executor(final @NotNull Executor executor) {
        this.executor = executor;
        return (B) this;
    }

    /**
     * Sets the data language format.
     *
     * @param dataLanguageFormat the data language format
     * @return this object (for method chaining)
     */
    public @NotNull B dataLanguageFormat(final @NotNull ConfigurationFormat dataLanguageFormat) {
        this.format = dataLanguageFormat;
        return (B) this;
    }

    /**
     * Sets logger.
     *
     * @param logger the logger
     * @return this object (for method chaining)
     */
    public @NotNull B logger(final @NotNull Logger logger) {
        this.logger = logger;
        return (B) this;
    }

}
