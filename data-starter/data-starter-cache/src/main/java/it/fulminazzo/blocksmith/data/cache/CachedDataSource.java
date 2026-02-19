package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

//TODO: documentation and scoping
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CachedDataSource<
        CS extends CacheRepositorySettings<CS>,
        S extends RepositorySettings
        > implements RepositoryDataSource<CachedRepositorySettings<CS, S>> {
    private final @NotNull CacheRepositoryDataSource<CS> cacheRepositoryDataSource;
    private final @NotNull RepositoryDataSource<S> repositoryDataSource;

    @Override
    public @NotNull <T, ID> Repository<T, ID> newRepository(
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
            @NotNull CacheRepositoryDataSource<CS> cacheDataSource,
            @NotNull RepositoryDataSource<S> repositoryDataSource
    ) {
        return new CachedDataSource<>(cacheDataSource, repositoryDataSource);
    }

}
