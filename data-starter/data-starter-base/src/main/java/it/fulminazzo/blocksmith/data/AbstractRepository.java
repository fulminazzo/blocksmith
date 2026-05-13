package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.ViolationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link Repository} with common checks
 * and support methods.
 *
 * @param <T> the type of the entities
 * @param <I> the type of the id of the entities (should be unique)
 * @param <E> the type of the {@link QueryEngine} responsible for executing internal queries
 * @see Repository
 * @see QueryEngine
 */
@RequiredArgsConstructor
public abstract class AbstractRepository<T, I, E extends QueryEngine<T, I>> implements Repository<T, I> {
    protected final @NotNull E queryEngine;
    @Getter
    protected final @NotNull EntityMapper<T, I> entityMapper;

    /**
     * Actual implementation of {@link #save(Object)}.
     * <br>
     * <b>For internal use only</b>: the entity will be already validated.
     *
     * @param entity the entity (already validated)
     * @return the saved entity (in case values are changed)
     */
    protected abstract @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity);

    /**
     * Actual implementation of {@link #findAll(Page)}.
     * <br>
     * <b>For internal use only</b>: the page number <b>must</b> be <b>greater than</b> or <b>equal to</b> {@code 0}
     * and pages must be <b>not empty</b>.
     *
     * @param page the page
     * @return the data
     */
    protected abstract @NotNull CompletableFuture<Collection<T>> findAllImpl(final @NotNull Page page);

    /**
     * Actual implementation of {@link #findAllById(Collection)}.
     * <br>
     * <b>For internal use only</b>: parameters must be <b>not empty</b> and elements must be <b>not</b> {@code null}.
     *
     * @param ids the ids
     * @return the data
     */
    protected abstract @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<I> ids);

    /**
     * Actual implementation of {@link #saveAll(Collection)}.
     * <br>
     * <b>For internal use only</b>: parameters must be <b>not empty</b> and elements must be <b>not</b> {@code null}.
     *
     * @param entities the entities
     * @return the saved entities (in case values are changed)
     */
    protected abstract @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities);

    /**
     * Actual implementation of {@link #deleteAll(Collection)}.
     * <br>
     * <b>For internal use only</b>: parameters must be <b>not empty</b> and elements must be <b>not</b> {@code null}.
     *
     * @param ids the ids
     * @return nothing
     */
    protected abstract @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<I> ids);

    /**
     * Actual implementation of {@link #delete(Object)}.
     * <br>
     * <b>For internal use only</b>.
     *
     * @param id the id
     * @return anything (results will be ignored)
     */
    protected abstract @NotNull CompletableFuture<?> deleteImpl(final @NotNull I id);

    @Override
    public @NotNull CompletableFuture<T> findByIdOrCreate(final @NotNull I id, final @NotNull T entity) {
        return findByIdOrCreate(id, i -> entity);
    }

    @Override
    public @NotNull CompletableFuture<T> findByIdOrCreate(final @NotNull I id, final @NotNull Function<I, T> supplier) {
        return findById(id).thenCompose(o -> o
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> save(supplier.apply(id)))
        );
    }

    @Override
    public final @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        try {
            Validator.validate(entity);
        } catch (ViolationException e) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        return saveImpl(entity);
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll(final @NotNull Page page) {
        if (page.getNumber() < 0 || page.getSize() < 1)
            return CompletableFuture.completedFuture(Collections.emptyList());
        else return findAllImpl(page);
    }

    @Override
    public final @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<I> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        else return findAllByIdImpl(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public final @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities) {
        if (entities.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        List<T> actualEntities = new ArrayList<>();
        try {
            for (T t : entities)
                if (t != null) {
                    Validator.validate(t);
                    actualEntities.add(t);
                }
        } catch (ViolationException e) {
            CompletableFuture<Collection<T>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        return saveAllImpl(actualEntities);
    }

    @Override
    public final @NotNull CompletableFuture<Void> deleteAll(final @NotNull Collection<I> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(null);
        else return deleteAllImpl(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()))
                .thenApply(r -> null);
    }

    @Override
    public final @NotNull CompletableFuture<Void> delete(final @NotNull I id) {
        return deleteImpl(id).thenApply(r -> null);
    }

}
