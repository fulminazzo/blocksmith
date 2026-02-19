package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CachedRepositorySettings<
        CS extends CacheRepositorySettings<CS>,
        S extends RepositorySettings
        > extends RepositorySettings {

    private final @NotNull CS cacheRepositorySettings;
    private final @NotNull S repositorySettings;

    public static <
            CS extends CacheRepositorySettings<CS>,
            S extends RepositorySettings
            > @NotNull CachedRepositorySettings<CS, S> combine(
            final @NotNull CS cacheRepositorySettings,
            final @NotNull S repositorySettings
    ) {
        return new CachedRepositorySettings<>(
                cacheRepositorySettings,
                repositorySettings
        );
    }

}
