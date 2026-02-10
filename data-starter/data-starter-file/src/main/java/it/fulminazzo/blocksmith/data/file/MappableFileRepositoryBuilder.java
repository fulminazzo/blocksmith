package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A special File repository builder with available data class
 * and support for the id mapper function.
 *
 * @param <T> the type of the data
 * @param <B> the type of the builder
 */
abstract class MappableFileRepositoryBuilder<T, B extends MappableFileRepositoryBuilder<T, B>>
        extends AbstractFileRepositoryBuilder<B> {
    protected final @NotNull Class<T> dataType;

    /**
     * Instantiates a new Mappable file repository builder.
     *
     * @param dataType the data type
     */
    public MappableFileRepositoryBuilder(final @NotNull Class<T> dataType) {
        this.dataType = dataType;
    }

    /**
     * Instantiates a new Mappable file repository builder.
     *
     * @param dataDirectory the data directory
     * @param executor      the executor
     * @param format        the format
     * @param logger        the logger
     * @param dataType      the data type
     */
    public MappableFileRepositoryBuilder(final @Nullable File dataDirectory,
                                         final @Nullable Executor executor,
                                         final @Nullable ConfigurationFormat format,
                                         final @Nullable Logger logger,
                                         final @NotNull Class<T> dataType) {
        super(dataDirectory, executor, format, logger);
        this.dataType = dataType;
    }

    /**
     * Sets the ID mapper function.
     * This function will be used to determine the ID of the data.
     *
     * @param <ID>     the type of the id
     * @param idMapper the function
     * @return the file repository builder with the mapper function
     */
    public <ID> @NotNull MappedFileRepositoryBuilder<T, ID> idMapper(final @NotNull Function<T, ID> idMapper) {
        return new MappedFileRepositoryBuilder<>(
                dataDirectory,
                executor,
                format,
                logger,
                dataType,
                idMapper
        );
    }

}
