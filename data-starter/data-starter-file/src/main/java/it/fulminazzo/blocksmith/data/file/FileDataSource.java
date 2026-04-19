package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Implementation of {@link RepositoryDataSource} for file-based repositories.
 * <br>
 * Persists data to the file system using a configurable file format (YAML, JSON, etc.).
 * Each entity is stored in a separate file named after its ID.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         ExecutorService executor = ...;
 *         FileDataSource dataSource = FileDataSource.create(executor);
 *         }</pre>
 *     </li>
 *     <li>creating a standard repository:
 *         <pre>{@code
 *         FileDataSource dataSource = ...;
 *         File dataDirectory = ...;
 *         Logger logger = LoggerFactory.getLogger(FileRepository.class);
 *         ConfigurationFormat format = ConfigurationFormat.YAML;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new FileRepositorySettings()
 *                         .withDataDirectory(dataDirectory)
 *                         .withLogger(logger)
 *                         .withFormat(format)
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom repository:
 *         <pre>{@code
 *         FileDataSource dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 engine -> new CustomFileRepository<>(engine),
 *                 new FileRepositorySettings()
 *                         .withDataDirectory(dataDirectory)
 *                         .withLogger(logger)
 *                         .withFormat(format)
 *         );
 *         }</pre>
 *         where CustomFileRepository extends FileRepository and adds custom behavior
 *         such as backup on save, or encryption of sensitive fields.
 *     </li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FileDataSource implements RepositoryDataSource<FileRepositorySettings> {
    private final @NotNull ExecutorService executor;

    @Override
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull FileRepositorySettings settings
    ) {
        return newRepository(
                e -> new FileRepository<>(e, entityMapper),
                settings
        );
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <ID>              the type of the id of the entities
     * @param repositoryBuilder the repository creation function
     * @param settings          the settings to build the repository with
     * @return the repository
     */
    public <T, ID, R extends FileRepository<T, ID>> @NotNull R newRepository(
            final @NotNull Function<FileQueryEngine<T, ID>, R> repositoryBuilder,
            final @NotNull FileRepositorySettings settings
    ) {
        FileQueryEngine<T, ID> engine = new FileQueryEngine<>(
                ConfigurationAdapter.newAdapter(settings.getLogger(), settings.getFormat()),
                settings.getFormat(),
                settings.getDataDirectory(),
                executor
        );
        return repositoryBuilder.apply(engine);
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
