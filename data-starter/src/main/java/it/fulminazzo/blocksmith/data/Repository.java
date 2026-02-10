package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A general repository for handling data.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
public interface Repository<T, ID> {

    /**
     * Gets the data with the associated id.
     *
     * @param id the id
     * @return the data
     */
    @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id);

    /**
     * Checks if a data with the associated id exists.
     *
     * @param id the id
     * @return <code>true</code> if it does
     */
    @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id);

    /**
     * Saves the given data.
     *
     * @param data the data
     * @return the saved data (in case values are changed)
     */
    @NotNull CompletableFuture<T> save(final @NotNull T data);

    /**
     * Deletes the data.
     *
     * @param id the id
     * @return nothing
     */
    @NotNull CompletableFuture<?> delete(final @NotNull ID id);

    /**
     * Gets all the data currently stored.
     *
     * @return the data
     */
    @NotNull CompletableFuture<Collection<T>> findAll();

    /**
     * Gets all the data with the associated it.
     *
     * @param ids the ids
     * @return the data
     */
    @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids);

    /**
     * Saves all the given entries.
     *
     * @param entries the entries
     * @return the saved entries (in case values are changed)
     */
    @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries);

    /**
     * Deletes all the data with the associated id.
     *
     * @param ids the ids
     * @return nothing
     */
    @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids);

    /**
     * Counts all the data currently stored.
     *
     * @return the amount
     */
    @NotNull CompletableFuture<Long> count();

}
