package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.QueryEngine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Pseudo-implementation of a Query engine for in-memory repositories.
 * <br>
 * Although this is not properly linked to a database,
 * the engine still provides a {@link #query(Function)} method for familiarity.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MemoryQueryEngine<T, ID> implements QueryEngine<T, ID> {
    private final @NotNull Map<ID, T> internalMap = new ExpirationMap<>(0);

    private final @NotNull Executor executor;

    /**
     * Executes the given function asynchronously.
     *
     * @param <R>           the type of the result
     * @param queryFunction the function
     * @return the result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<Map<ID, T>, R> queryFunction
    ) {
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(internalMap), executor);
    }

    /**
     * Sets the expiration time when saving an entity.
     *
     * @param expiryInMillis the expiration time (in milliseconds)
     */
    public void setExpiry(final @Range(from = 0, to = Long.MAX_VALUE) long expiryInMillis) {
        ((ExpirationMap<ID, T>) internalMap).setExpiry(expiryInMillis);
    }

}
