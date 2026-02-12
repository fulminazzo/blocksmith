package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract implementation of {@link Repository} with common checks
 * and support methods.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
@RequiredArgsConstructor
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {
    protected final @NotNull EntityMapper<T, ID> entityMapper;

    @Override
    public @NotNull CompletableFuture<Void> delete(final @NotNull ID id) {
        return deleteImpl(id).thenApply(r -> null);
    }

    /**
     * Internal implementation of {@link #delete(Object)}.
     *
     * @param id the id
     * @return anything (result will be ignored)
     * @deprecated FOR INTERNAL USE ONLY
     */
    protected abstract @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id);

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
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities) {
        if (entities.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return saveAllImpl(entities);
    }

    /**
     * Internal implementation of {@link #saveAll(Collection)}.
     *
     * @param entities the entities
     * @return the saved entities (in case values are changed)
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities);

    @Override
    public @NotNull CompletableFuture<Void> deleteAll(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(null);
        return deleteAllImpl(ids).thenApply(r -> null);
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
