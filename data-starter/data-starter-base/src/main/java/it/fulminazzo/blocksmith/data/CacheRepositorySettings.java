package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Duration;

/**
 * Abstract implementation of {@link RepositorySettings} for {@link CacheRepository}.
 *
 * @param <S> the type of this settings
 */
@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public abstract class CacheRepositorySettings<S extends CacheRepositorySettings<S>> extends RepositorySettings {
    @Getter
    private @Nullable Duration ttl;

    /**
     * Sets the expiration time of the stored entities.
     *
     * @param expiry the expiration time
     * @return this object (for method chaining)
     */
    public @NotNull S withTtl(final
                              @Range(from = 0, to = Long.MAX_VALUE)
                              @PositiveOrZero(exceptionMessage = "cache expire time must be at least 0")
                              @NotNull
                              Duration expiry) {
        Validator.validateMethod(expiry);
        this.ttl = expiry;
        return (S) this;
    }

}
