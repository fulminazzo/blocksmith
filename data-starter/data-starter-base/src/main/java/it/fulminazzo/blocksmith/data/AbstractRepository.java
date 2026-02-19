package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.util.ValidationUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link Repository} with common checks
 * and support methods.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 * @param <E>  the type of the {@link QueryEngine} responsible for executing internal queries
 */
@RequiredArgsConstructor
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class AbstractRepository<T, ID, E extends QueryEngine<T, ID>> implements Repository<T, ID> {
    protected final @NotNull E queryEngine;
    @Getter
    protected final @NotNull EntityMapper<T, ID> entityMapper;

    @Override
    public final @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        try {
            ValidationUtils.validate(entity);
        } catch (ValidationUtils.ViolationException e) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        return saveImpl(entity);
    }

    /**
     * Internal implementation of {@link #save(Object)}.
     *
     * @param entity the entity (already validated)
     * @return the saved entity (in case values are changed)
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity);

    @Override
    public final @NotNull CompletableFuture<Void> delete(final @NotNull ID id) {
        return deleteImpl(id).thenApply(r -> null);
    }

    /**
     * Internal implementation of {@link #delete(Object)}.
     *
     * @param id the id
     * @return anything (result will be ignored)
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id);

    @Override
    public final @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return findAllByIdImpl(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * Internal implementation of {@link #findAllById(Collection)}.
     *
     * @param ids the ids
     * @return the data
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY AND ELEMENTS NOT <code>null</code>
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids);

    @Override
    public final @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities) {
        if (entities.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        List<T> actualEntities = new ArrayList<>();
        try {
            for (T t : entities)
                if (t != null) {
                    ValidationUtils.validate(t);
                    actualEntities.add(t);
                }
        } catch (ValidationUtils.ViolationException e) {
            CompletableFuture<Collection<T>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        return saveAllImpl(actualEntities);
    }

    /**
     * Internal implementation of {@link #saveAll(Collection)}.
     *
     * @param entities the entities
     * @return the saved entities (in case values are changed)
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY AND ELEMENTS NOT <code>null</code>
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities);

    @Override
    public final @NotNull CompletableFuture<Void> deleteAll(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(null);
        return deleteAllImpl(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()))
                .thenApply(r -> null);
    }

    /**
     * Internal implementation of {@link #deleteAll(Collection)}.
     *
     * @param ids the ids
     * @return nothing
     * @deprecated FOR INTERNAL USE ONLY, PARAMETER MUST BE NOT EMPTY AND ELEMENTS NOT <code>null</code>
     */
    @Deprecated
    protected abstract @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids);

}
