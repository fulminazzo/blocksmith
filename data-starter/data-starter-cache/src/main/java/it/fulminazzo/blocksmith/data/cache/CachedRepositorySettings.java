package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * Repository settings for cached repositories.
 *
 * @param <C> the type of the cache repository settings
 * @param <S> the type of the repository settings
 * @see CachedRepository
 * @see CachedDataSource
 * @see HybridCachedDataSource
 */
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CachedRepositorySettings<
        C extends CacheRepositorySettings<C>,
        S extends RepositorySettings
        > extends RepositorySettings {

    private final @NotNull C cacheRepositorySettings;
    private final @NotNull S repositorySettings;

    /**
     * Instantiates a new Cached repository settings.
     *
     * @param <C>                     the type of the cache repository settings
     * @param <S>                     the type of the repository settings
     * @param cacheRepositorySettings the repository settings for the repository to use as cache
     * @param repositorySettings      the repository settings for the main repository
     * @return the cached repository settings
     */
    public static <
            C extends CacheRepositorySettings<C>,
            S extends RepositorySettings
            > @NotNull CachedRepositorySettings<C, S> combine(
            final @NotNull C cacheRepositorySettings,
            final @NotNull S repositorySettings
    ) {
        return new CachedRepositorySettings<>(
                cacheRepositorySettings,
                repositorySettings
        );
    }

}
