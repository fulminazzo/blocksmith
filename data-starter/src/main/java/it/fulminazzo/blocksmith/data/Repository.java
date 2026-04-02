package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A general repository for handling entities in a data source.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public interface Repository<T, ID> {

    /**
     * Gets the entity with the associated id.
     *
     * @param id the id
     * @return the entity
     */
    @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id);

    /**
     * Attempts to find the entity with the associated it.
     * If it was not found, it will be created.
     *
     * @param id the id
     * @param entity the entity (will be stored in the database if existing not found)
     * @return the entity
     */
    @NotNull CompletableFuture<T> findByIdOrCreate(final @NotNull ID id, final @NotNull T entity);

    /**
     * Attempts to find the entity with the associated it.
     * If it was not found, it will be created.
     *
     * @param id the id
     * @param supplier the supplier to create and store a new entity from the given id
     * @return the entity
     */
    @NotNull CompletableFuture<T> findByIdOrCreate(final @NotNull ID id, final @NotNull Function<ID, T> supplier);

    /**
     * Checks if an entity with the associated id exists.
     *
     * @param id the id
     * @return <code>true</code> if it does
     */
    @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id);

    /**
     * Saves the given entity.
     *
     * @param entity the entity
     * @return the saved entity (in case values are changed)
     */
    @NotNull CompletableFuture<T> save(final @NotNull T entity);

    /**
     * Deletes the entities.
     *
     * @param id the id
     * @return nothing
     */
    @NotNull CompletableFuture<Void> delete(final @NotNull ID id);

    /**
     * Gets all the entities currently stored.
     *
     * @return the entities
     */
    @NotNull CompletableFuture<Collection<T>> findAll();

    /**
     * Gets all the entities currently stored.
     *
     * @param page the page to display
     * @return the entities
     */
    @NotNull CompletableFuture<Collection<T>> findAll(final @NotNull Page page);

    /**
     * Gets all the entities with the associated it.
     *
     * @param ids the ids
     * @return the filtered entities
     */
    @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids);

    /**
     * Saves all the given entities.
     *
     * @param entities the entities
     * @return the saved entities (in case values are changed)
     */
    @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities);

    /**
     * Deletes all the entities with the associated id.
     *
     * @param ids the ids
     * @return nothing
     */
    @NotNull CompletableFuture<Void> deleteAll(final @NotNull Collection<ID> ids);

    /**
     * Counts all the entities currently stored.
     *
     * @return the amount
     */
    @NotNull CompletableFuture<Long> count();

}
