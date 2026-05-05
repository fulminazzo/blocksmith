package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

/**
 * An expiring interface is a special {@link Set} whose elements are subject to expiration.
 * Each can be defined with a time-to-live (TTL) period after which they will not be present anymore.
 *
 * @param <E> the type of the elements
 */
public interface ExpiringSet<E> extends Set<E> {

    /**
     * Adds a new element in the set.
     * <br>
     * If the element is already present, its TTL will be renewed.
     *
     * @param element the element to add
     * @param ttl the time-to-live (after which it will expire)
     * @return {@code true} if the element was added, {@code false} if it was already present
     */
    boolean add(final @Nullable E element, final @NotNull Duration ttl);

    /**
     * Adds a new element in the set.
     * <br>
     * If the element is already present, its TTL will be renewed.
     *
     * @param element the element to add
     * @param ttl the time-to-live (after which it will expire) in milliseconds
     * @return {@code true} if the element was added, {@code false} if it was already present
     */
    boolean add(final @Nullable E element, final long ttl);

    /**
     * Adds a new element in the set.
     * The element will have no expiration time.
     *
     * @param element the element to add
     * @return {@code true} if the element was added, {@code false} if it was already present
     */
    @Override
    boolean add(final @Nullable E element);

    /**
     * Adds all the elements of the given set to the current one.
     *
     * @param set the set to take elements from
     * @return {@code true} if the set was modified, {@code false} if it was not modified
     */
    boolean addAll(final @NotNull ExpiringSet<? extends E> set);

    /**
     * Adds all the elements of the given collection to the current one.
     * Each element will have the same expiration time.
     *
     * @param collection the collection to take elements from
     * @param ttl the time-to-live (after which the elements will expire)
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    boolean addAll(final @NotNull Collection<? extends E> collection, final @NotNull Duration ttl);

    /**
     * Adds all the elements of the given collection to the current one.
     * Each element will have the same expiration time.
     *
     * @param collection the collection to take elements from
     * @param ttl the time-to-live (after which the elements will expire) in milliseconds
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    boolean addAll(final @NotNull Collection<? extends E> collection, final long ttl);

    /**
     * Adds all the elements of the given collection to the current one.
     * If the collection is not a {@link ExpiringSet}, every element will have no expiration time.
     *
     * @param collection the collection to take elements from
     * @return {@code true} if the collection was modified, {@code false} if it was not modified
     */
    @Override
    boolean addAll(final @NotNull Collection<? extends E> collection);

    /**
     * Gets the remaining time-to-live of the element.
     *
     * @param key the key
     * @return the TTL ({@code null} if not present)
     */
    @Nullable Duration getTtl(final @Nullable E key);

    /**
     * Prints out the contents of this set.
     * <br>
     * Elements will be printed in the format {@code <element>}.
     * <br>
     * If an element is <b>never expiring</b>, it will be printed as {@code <element> (!)}.
     * <br>
     * If an element is <b>expired</b> but not yet removed, it will be printed as {@code <element> (*)}.
     *
     * @return the string representation of this set
     */
    @Override
    @NotNull String toString();

}
