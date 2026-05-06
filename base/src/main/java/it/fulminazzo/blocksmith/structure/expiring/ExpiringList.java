package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An expiring list is a special {@link List} whose elements are subject to expiration.
 * Each can be defined with a time-to-live (TTL) period after which they will not be present anymore.
 *
 * @param <E> the type of the elements
 */
public interface ExpiringList<E> extends List<E>, ExpiringCollection<E> {

    /**
     * Adds an element in the list at the given index.
     * All the elements after the index will be shifted to the right.
     *
     * @param index   the index at which to insert the element
     * @param element the element to add
     * @param ttl     the time-to-live (after which it will expire)
     */
    void add(final int index, final @Nullable E element, final @NotNull Duration ttl);

    /**
     * Adds an element in the list at the given index.
     * All the elements after the index will be shifted to the right.
     *
     * @param index   the index at which to insert the element
     * @param element the element to add
     * @param ttl     the time-to-live (after which it will expire) in milliseconds
     */
    void add(final int index, final @Nullable E element, final long ttl);

    /**
     * Adds an element in the list at the given index.
     * All the elements after the index will be shifted to the right.
     *
     * @param index   the index at which to insert the element
     * @param element the element to add
     */
    @Override
    void add(final int index, final @Nullable E element);

    /**
     * Adds all the elements of the given list to the current one at the given index.
     *
     * @param index      the index at which to insert the elements
     * @param collection the collection to take elements from
     * @return {@code true} if the list was modified, {@code false} if it was not modified
     */
    boolean addAll(final int index, final @NotNull ExpiringCollection<? extends E> collection);

    /**
     * Adds all the elements of the given collection to the current one at the given index.
     * Each element will have the same expiration time.
     *
     * @param index      the index at which to insert the elements
     * @param collection the collection to take elements from
     * @param ttl        the time-to-live (after which the elements will expire)
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    boolean addAll(final int index, final @NotNull Collection<? extends E> collection, final @NotNull Duration ttl);

    /**
     * Adds all the elements of the given collection to the current one at the given index.
     * Each element will have the same expiration time.
     *
     * @param index      the index at which to insert the elements
     * @param collection the collection to take elements from
     * @param ttl        the time-to-live (after which the elements will expire) in milliseconds
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    boolean addAll(final int index, final @NotNull Collection<? extends E> collection, final long ttl);

    /**
     * Adds all the elements of the given collection to the current one at the given index.
     * If the collection is not a {@link ExpiringList}, every element will have no expiration time.
     *
     * @param index      the index at which to insert the elements
     * @param collection the collection to take elements from
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    @Override
    boolean addAll(final int index, final @NotNull Collection<? extends E> collection);

    /**
     * Sets the element at the given index.
     *
     * @param index   the index of the element to set
     * @param element the new element
     * @param ttl     the time-to-live (after which it will expire)
     * @return the previous element at the given index
     */
    E set(final int index, final @Nullable E element, final @NotNull Duration ttl);

    /**
     * Sets the element at the given index.
     *
     * @param index   the index of the element to set
     * @param element the new element
     * @param ttl     the time-to-live (after which it will expire) in milliseconds
     * @return the previous element at the given index
     */
    E set(final int index, final @Nullable E element, final long ttl);

    /**
     * Sets the element at the given index.
     *
     * @param index   the index of the element to set
     * @param element the new element
     * @return the previous element at the given index
     */
    @Override
    E set(final int index, final @Nullable E element);

    /**
     * Returns a sublist of the elements in this list between
     * the specified {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * @param fromIndex the index of the first element to include in the sublist
     * @param toIndex   the index of the first element to exclude from the sublist
     * @return a sublist of the elements in this list between the specified indices
     */
    @Override
    @NotNull ExpiringList<E> subList(int fromIndex, int toIndex);

    /**
     * Initializes a new passive ExpirationList.
     * <br>
     * The elements will persist in memory <b>FOREVER</b>,
     * until the user <b>manually</b> removes them.
     *
     * @param <E> the type of the elements
     * @return the list
     */
    static <E> @NotNull ExpiringList<E> passive() {
        return new PassiveExpiringList<>();
    }

    /**
     * Initializes a new lazy ExpirationList.
     * <br>
     * The elements will persist in memory until an operation has been done
     * to the list itself, at which point the expired elements will be removed.
     *
     * @param <E> the type of the elements
     * @return the list
     */
    static <E> @NotNull ExpiringList<E> lazy() {
        return new LazyExpiringList<>();
    }

    /**
     * Initializes a new scheduled ExpirationList.
     * <br>
     * The elements will be periodically check for expiration and be removed.
     *
     * @param <E>          the type of the elements
     * @param scheduler    the scheduler that will handle the periodical removal
     * @param taskInterval the interval upon which to check expirations
     * @return the list
     */
    static <E> @NotNull ExpiringList<E> scheduled(final @NotNull ScheduledExecutorService scheduler,
                                                  final @NotNull Duration taskInterval) {
        return new ScheduledExpiringList<>(scheduler, taskInterval);
    }

}
