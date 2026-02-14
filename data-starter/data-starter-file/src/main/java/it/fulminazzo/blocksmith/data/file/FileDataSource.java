package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link RepositoryDataSource} for file-based repositories.
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         ExecutorService executor = ...;
 *         FileDataSource dataSource = FileDataSource.create(executor);
 *         }</pre>
 *     </li>
 *     <li>creating a new repository:
 *         <pre>{@code
 *         FileDataSource dataSource = ...;
 *         Class<?> dataType = ...;
 *         File dataDirectory = ...;
 *         Logger logger = LoggerFactory.getLogger(FileRepository.class);
 *         ConfigurationFormat format = ConfigurationFormat.YAML;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 dataType,
 *                 dataDirectory,
 *                 logger,
 *                 format
 *         );
 *         }</pre>
 *         or, for more control:
 *         <pre>{@code
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(dataType, "idFieldName"),
 *                 dataDirectory,
 *                 logger,
 *                 format
 *         );
 *         }</pre>
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FileDataSource implements RepositoryDataSource {
    private final @NotNull ExecutorService executor;

    /**
     * Creates a new repository.
     *
     * @param <T>           the type of the entities
     * @param <ID>          the type of the id of the entities
     * @param entityType    the entity Java class
     * @param dataDirectory the directory where all the data is stored
     * @param logger        the logger
     * @param format        the configuration format to use for storing
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull File dataDirectory,
            final @NotNull Logger logger,
            final @NotNull ConfigurationFormat format
    ) {
        return newRepository(EntityMapper.create(entityType), dataDirectory, logger, format);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>           the type of the entities
     * @param <ID>          the type of the id of the entities
     * @param entityMapper  the entity mapper
     * @param dataDirectory the directory where all the data is stored
     * @param logger        the logger
     * @param format        the configuration format to use for storing
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull File dataDirectory,
            final @NotNull Logger logger,
            final @NotNull ConfigurationFormat format
    ) {
        FileQueryEngine<T, ID> engine = new FileQueryEngine<>(
                ConfigurationAdapter.newAdapter(logger, format),
                format,
                dataDirectory,
                executor
        );
        return new FileRepository<>(engine, entityMapper);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    /**
     * Creates a new File data source.
     *
     * @param executor the executor
     * @return the file data source
     */
    public static @NotNull FileDataSource create(final @NotNull ExecutorService executor) {
        return new FileDataSource(executor);
    }

}
