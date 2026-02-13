package it.fulminazzo.blocksmith.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A general repository for handling entities in a datasource.
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
