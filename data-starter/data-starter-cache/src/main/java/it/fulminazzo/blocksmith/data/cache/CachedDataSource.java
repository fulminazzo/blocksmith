package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

//TODO: documentation and scoping
@RequiredArgsConstructor
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
                ds -> ds.newRepository(entityMapper, settings.getCachedRepositorySettings()),
                ds -> ds.newRepository(entityMapper, settings.getRepositorySettings()),
                (cr, r) -> new CachedRepository<>(entityMapper, cr, r)
        );
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <ID>              the type of the id of the entities
     * @param cacheRepositoryBuilder    the cache repository creation function
     * @param internalRepositoryBuilder the internal repository creation function
     * @param repositoryBuilder the repository creation function
     * @return the repository
     */
    public <R extends CachedRepository<T, ID>, T, ID> @NotNull R newRepository(
            final @NotNull Function<CacheRepositoryDataSource<CS>, CacheRepository<T, ID>> cacheRepositoryBuilder,
            final @NotNull Function<RepositoryDataSource<S>, Repository<T, ID>> internalRepositoryBuilder,
            final @NotNull BiFunction<CacheRepository<T, ID>, Repository<T, ID>, R> repositoryBuilder
            ) {
        CacheRepository<T, ID> cacheRepository = cacheRepositoryBuilder.apply(cacheRepositoryDataSource);
        Repository<T, ID> repository = internalRepositoryBuilder.apply(repositoryDataSource);
        return repositoryBuilder.apply(cacheRepository, repository);
    }

    @Override
    public void close() throws IOException {
        cacheRepositoryDataSource.close();
        repositoryDataSource.close();
    }

}
