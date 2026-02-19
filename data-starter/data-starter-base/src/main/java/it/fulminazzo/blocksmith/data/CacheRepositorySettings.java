package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.util.ValidationUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Abstract implementation of {@link RepositorySettings} for {@link CacheRepository}.
 *
 * @param <S> the type of this settings
 */
@SuppressWarnings("unchecked")
public abstract class CacheRepositorySettings<S extends CacheRepositorySettings<S>> extends RepositorySettings {
    @Getter
    private @Nullable Duration ttl;

    /**
     * Sets the expiration time of the stored entities.
     *
     * @param expiry the expiration time
     * @return this object (for method chaining)
     */
    public @NotNull S withTtl(final @NotNull Duration expiry) {
        ValidationUtils.checkPositive(expiry.toMillis(), "cache expire time");
        this.ttl = expiry;
        return (S) this;
    }

}
