package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.util.ValidationUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Abstract implementation of {@link RepositorySettings} for {@link CacheRepository}.
 *
 * @param <S> the type of this settings
 */
@SuppressWarnings("unchecked")
public abstract class CacheRepositorySettings<S extends CacheRepositorySettings<S>> extends RepositorySettings {
    @Getter
    private long expiryInMillis;

    /**
     * With expiry in millis cache repository settings.
     *
     * @param expiryInMillis the expiry in millis
     * @return the cache repository settings
     */
    public @NotNull S withExpiryInMillis(final @Range(from = 0, to = Long.MAX_VALUE) long expiryInMillis) {
        ValidationUtils.checkPositive(expiryInMillis, "cache expire time in milliseconds");
        this.expiryInMillis = expiryInMillis;
        return (S) this;
    }

}
