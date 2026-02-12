package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract implementation of {@link Repository} with common checks
 * and support methods.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return findAllByIdImpl(ids);
    }

    /**
     * Internal implementation of {@link #findAllById(Collection)}.
     *
     * @param ids the ids
     * @return the data
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids);

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries) {
        if (entries.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return saveAllImpl(entries);
    }

    /**
     * Internal implementation of {@link #saveAll(Collection)}.
     *
     * @param entries the entries
     * @return the saved entries (in case values are changed)
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entries);

    @Override
    public @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return deleteAllImpl(ids);
    }

    /**
     * Internal implementation of {@link #deleteAll(Collection)}.
     *
     * @param ids the ids
     * @return nothing
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids);

}
