package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of {@link RepositoryDataSource} for cached repositories.
 * Every query to the main repository will be passed to a cache that will be
 * updated accordingly.
 * <br>
 * The following examples will utilize a <b>SQL</b> as main repository (with the support of the
 * <a href="https://www.jooq.org/doc/latest/manual/code-generation/codegen-execution/codegen-gradle/">jOOQ generator plugin</a>
 * and <b>Redis</b> as cache.
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         CachedDataSource<?, ?> dataSource = CachedDataSource.create(
 *                 RedisDataSource.builder()
 *                         .uri(b -> b.withHost("0.0.0.0").withPort(6379))
 *                         .mapper(Mappers.JSON)
 *                         .build(),
 *                 SqlDataSource.builder()
 *                         .database("database")
 *                         .username("root")
 *                         .password("super-secure-password-should-use-an-env-variable")
 *                         .databaseType(DatabaseType.MARIADB)
 *                         .host("0.0.0.0")
 *                         .port(3306)
 *                         .mysql()
 *                         .build()
 *         );
 *         }</pre>
 *     </li>
 *     <li>creation of hybrid (repositories will look up an in-memory cache before querying the cache):</li>
 *         <pre>{@code
 *         HybridCachedDataSource<?, ?> dataSource = CachedDataSource.hybrid(
 *                 MemoryDataSource.create(Executors.newSingleThreadExecutor()),
 *                 RedisDataSource.builder()
 *                         .uri(b -> b.withHost("0.0.0.0").withPort(6379))
 *                         .mapper(Mappers.JSON)
 *                         .build(),
 *                 SqlDataSource.builder()
 *                         .database("database")
 *                         .username("root")
 *                         .password("super-secure-password-should-use-an-env-variable")
 *                         .databaseType(DatabaseType.MARIADB)
 *                         .host("0.0.0.0")
 *                         .port(3306)
 *                         .mysql()
 *                         .build()
 *         );
 *         }</pre>
 *     <li>creating a standard repository:
 *         <pre>{@code
 *         CachedDataSource<RedisRepositorySettings, SqlRepositorySettings> dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 CachedRepositorySettings.combine(
 *                         new RedisRepositorySettings()
 *                                 .withDatabaseName("database")
 *                                 .withCollectionName("users")
 *                                 .withTtl(Duration.ofMinutes(30)),
 *                         new SqlRepositorySettings()
 *                                 .withTable(Tables.USERS)
 *                                 .withIdColumn(Tables.USERS.ID)
 *                 )
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom repository:
 *         <pre>{@code
 *         CachedDataSource<RedisRepositorySettings, SqlRepositorySettings> dataSource = ...;
 *         EntityMapper<?, ?> entityMapper = EntityMapper.create(User.class);
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 ds -> ((RedisDataSource) ds).newRepository(
 *                         e -> new CustomRedisRepository(e),
 *                         new RedisRepositorySettings()
 *                                 .withEntityMapper(entityMapper)
 *                                 .withDatabaseName("database")
 *                                 .withCollectionName("users")
 *                                 .withTtl(Duration.ofMinutes(30))
 *                 ),
 *                 ds -> ((SqlDataSource) ds).newRepository(
 *                         e -> new CustomSqlRepository(e),
 *                         new SqlRepositorySettings()
 *                                 .withTable(Tables.USERS)
 *                                 .withIdColumn(Tables.USERS.ID)
 *                 ),
 *                 (cr, r) -> new CustomCachedRepository(cr, r)
 *         );
 *         }</pre>
 *     </li>
 * </ul>
 *
 * @param <CS> the repository settings of the cache repository data source
 * @param <S>  the repository settings of the internal repository data source
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CachedDataSource<
        CS extends CacheRepositorySettings<CS>,
        S extends RepositorySettings
        > implements RepositoryDataSource<CachedRepositorySettings<CS, S>> {
    private final @NotNull CacheRepositoryDataSource<CS> cacheRepositoryDataSource;
    private final @NotNull RepositoryDataSource<S> repositoryDataSource;

    @Override
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull CachedRepositorySettings<CS, S> settings
    ) {
        return newRepository(
                ds -> ds.newRepository(entityMapper, settings.getCacheRepositorySettings()),
                ds -> ds.newRepository(entityMapper, settings.getRepositorySettings()),
                (cr, r) -> new CachedRepository<>(cr, r, entityMapper)
        );
    }

    /**
     * Creates a new custom repository.
     *
     * @param <T>                       the type of the entities
     * @param <ID>                      the type of the id of the entities
     * @param <CR>                      the type of the cache repository
     * @param <IR>                      the type of the internal repository
     * @param <R>                       the type of the repository
     * @param cacheRepositoryBuilder    the cache repository creation function
     * @param internalRepositoryBuilder the internal repository creation function
     * @param repositoryBuilder         the repository creation function
     * @return the repository
     */
    public <T, ID,
            CR extends CacheRepository<T, ID>,
            IR extends Repository<T, ID>,
            R extends CachedRepository<T, ID>
            > @NotNull R newRepository(
            final @NotNull Function<CacheRepositoryDataSource<CS>, CR> cacheRepositoryBuilder,
            final @NotNull Function<RepositoryDataSource<S>, IR> internalRepositoryBuilder,
            final @NotNull BiFunction<CR, IR, R> repositoryBuilder
    ) {
        CR cacheRepository = cacheRepositoryBuilder.apply(cacheRepositoryDataSource);
        IR repository = internalRepositoryBuilder.apply(repositoryDataSource);
        return repositoryBuilder.apply(cacheRepository, repository);
    }

    @Override
    public void close() throws IOException {
        cacheRepositoryDataSource.close();
        repositoryDataSource.close();
    }

    /**
     * Creates a new Hybrid Cached data source.
     * <br>
     * A Hybrid Cached data source is a special {@link CachedDataSource} that uses two caches.
     * The lookup logic is the following:
     * <ol>
     *     <li>a {@link it.fulminazzo.blocksmith.data.memory.MemoryRepository} is queried for the resource;</li>
     *     <li>if not found, the {@link CachedRepository} is queried for the resource;</li>
     *     <li>if not found, the actual {@link Repository} is queried.</li>
     * </ol>
     * This process allows for faster lookups when querying multiple data.
     *
     * @param <CS>                 the type of the settings for the cache repository
     * @param <S>                  the type of the settings for the actual repository
     * @param memoryDataSource     the in-memory repositories data source
     * @param cacheDataSource      the cache repositories data source
     * @param repositoryDataSource the actual repositories data source
     * @return the hybrid data source
     */
    public static <
            CS extends CacheRepositorySettings<CS>,
            S extends RepositorySettings
            > @NotNull HybridCachedDataSource<CS, S> hybrid(
            final @NotNull MemoryDataSource memoryDataSource,
            final @NotNull CacheRepositoryDataSource<CS> cacheDataSource,
            final @NotNull RepositoryDataSource<S> repositoryDataSource
    ) {
        return new HybridCachedDataSource<>(
                memoryDataSource,
                create(cacheDataSource, repositoryDataSource)
        );
    }

    /**
     * Creates a new Cached data source.
     *
     * @param <CS>                 the type of the settings for the cache repository
     * @param <S>                  the type of the settings for the actual repository
     * @param cacheDataSource      the cache repositories data source
     * @param repositoryDataSource the actual repositories data source
     * @return the data source
     */
    public static <
            CS extends CacheRepositorySettings<CS>,
            S extends RepositorySettings
            > @NotNull CachedDataSource<CS, S> create(
            final @NotNull CacheRepositoryDataSource<CS> cacheDataSource,
            final @NotNull RepositoryDataSource<S> repositoryDataSource
    ) {
        return new CachedDataSource<>(cacheDataSource, repositoryDataSource);
    }

}
