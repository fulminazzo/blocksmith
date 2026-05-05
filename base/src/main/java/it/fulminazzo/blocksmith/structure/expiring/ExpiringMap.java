package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An expiring map is a special {@link Map} whose elements are subject to expiration.
 * Each can be defined with a time-to-live (TTL) period after which they will not be present anymore.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public interface ExpiringMap<K, V> extends Map<K, V> {

    /**
     * Adds a new key-value pair in the map.
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the time-to-live (after which it will expire)
     * @return the previous value present in the map
     */
    @Nullable V put(final @Nullable K key, final @Nullable V value, final @NotNull Duration ttl);

    /**
     * Adds a new key-value pair in the map.
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the time-to-live (after which it will expire) in milliseconds
     * @return the previous value present in the map
     */
    @Nullable V put(final @Nullable K key, final @Nullable V value, final long ttl);

    /**
     * Adds a new key-value pair in the map.
     * The pair will have no expiration time.
     *
     * @param key   the key
     * @param value the value
     * @return the previous value present in the map
     */
    @Override
    @Nullable V put(final @Nullable K key, final @Nullable V value);

    /**
     * Adds a key-value pair in the map (if not already present).
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the time-to-live (after which it will expire)
     * @return the value of the map if present, otherwise the value parameter
     */
    @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value, final @NotNull Duration ttl);

    /**
     * Adds a key-value pair in the map (if not already present).
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the time-to-live (after which it will expire) in milliseconds
     * @return the value of the map if present, otherwise the value parameter
     */
    @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value, final long ttl);

    /**
     * Adds a key-value pair in the map (if not already present).
     * The pair will have no expiration time.
     *
     * @param key   the key
     * @param value the value
     * @return the value of the map if present, otherwise the value parameter
     */
    @Override
    @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value);

    /**
     * If a pair with the given key and old value is present, it is replaced with the new value.
     *
     * @param key      the key
     * @param oldValue the old value
     * @param newValue the new value
     * @param ttl      the time-to-live (after which it will expire)
     * @return {@code true} if the replacement was successful, {@code false} if no element matching the pair was found
     */
    boolean replace(final @Nullable K key,
                    final @Nullable V oldValue,
                    final @Nullable V newValue,
                    final @NotNull Duration ttl);

    /**
     * If a pair with the given key and old value is present, it is replaced with the new value.
     *
     * @param key      the key
     * @param oldValue the old value
     * @param newValue the new value
     * @param ttl      the time-to-live (after which it will expire) in milliseconds
     * @return {@code true} if the replacement was successful, {@code false} if no element matching the pair was found
     */
    boolean replace(final @Nullable K key,
                    final @Nullable V oldValue,
                    final @Nullable V newValue,
                    final long ttl);

    /**
     * If a pair with the given key and old value is present, it is replaced with the new value.
     * The time-to-live is <b>not</b> refreshed.
     *
     * @param key      the key
     * @param oldValue the old value
     * @param newValue the new value
     * @return {@code true} if the replacement was successful,
     * {@code false} if no element matching the pair was found
     */
    @Override
    boolean replace(final @Nullable K key, final @Nullable V oldValue, final @Nullable V newValue);

    /**
     * If a pair with the given key is present, it is replaced with the new value.
     *
     * @param key   the key
     * @param value the new value
     * @return the previous value present in the map
     */
    @Override
    @Nullable V replace(final @Nullable K key, final @Nullable V value);

    /**
     * Adds a new key-value pair computed from the given function in the map
     * only if an element with the given key is not present.
     *
     * @param key             the key
     * @param mappingFunction the function to get the new value from
     * @param ttl             the time-to-live (after which it will expire)
     * @return the value of the map if present, otherwise the value parameter
     */
    @Nullable V computeIfAbsent(final @Nullable K key,
                                final @NotNull Function<? super K, ? extends V> mappingFunction,
                                final @NotNull Duration ttl);

    /**
     * Adds a new key-value pair computed from the given function in the map
     * only if an element with the given key is not present.
     *
     * @param key             the key
     * @param mappingFunction the function to get the new value from
     * @param ttl             the time-to-live (after which it will expire) in milliseconds
     * @return the value of the map if present, otherwise the value parameter
     */
    @Nullable V computeIfAbsent(final @Nullable K key,
                                final @NotNull Function<? super K, ? extends V> mappingFunction,
                                final long ttl);

    /**
     * Adds a new key-value pair computed from the given function in the map
     * only if an element with the given key is not present.
     * The pair will have no expiration time.
     *
     * @param key             the key
     * @param mappingFunction the function to get the new value from
     * @return the value of the map if present, otherwise the value parameter
     */
    @Override
    @Nullable V computeIfAbsent(final @Nullable K key, final @NotNull Function<? super K, ? extends V> mappingFunction);

    /**
     * Updates an element in the map with a new value computed from the given function.
     * The time-to-live is <b>not</b> refreshed.
     *
     * @param key               the key
     * @param remappingFunction the function to get the new value from
     * @return the value of the map if present, otherwise {@code null}
     */
    @Override
    @Nullable V computeIfPresent(final @Nullable K key, final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * Adds, updates or removes a key-value pair in the map.
     *
     * @param key               the key
     * @param remappingFunction the function to update the value from
     * @param ttl               the time-to-live (after which it will expire)
     * @return the previous value present in the map
     */
    @Nullable V compute(final @Nullable K key,
                        final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction,
                        final @NotNull Duration ttl);

    /**
     * Adds, updates or removes a key-value pair in the map.
     *
     * @param key               the key
     * @param remappingFunction the function to update the value from
     * @param ttl               the time-to-live (after which it will expire) in milliseconds
     * @return the previous value present in the map
     */
    @Nullable V compute(final @Nullable K key,
                        final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction,
                        final long ttl);

    /**
     * Adds, updates or removes a key-value pair in the map.
     * If the pair was added, it will have no expiration time.
     * If the pair was updated, it will keep its expiration time.
     *
     * @param key               the key
     * @param remappingFunction the function to update the value from
     * @return the previous value present in the map
     */
    @Override
    @Nullable V compute(final @Nullable K key, final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * Updates an element in the map.
     *
     * @param key               the key
     * @param value             the value inserted if the pair was not present
     * @param remappingFunction the function used to update an existing pair value
     * @param ttl               the time-to-live (after which it will expire)
     * @return the updated value
     */
    @Nullable V merge(final @Nullable K key, final @NotNull V value,
                      final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction,
                      final @NotNull Duration ttl);

    /**
     * Updates an element in the map.
     *
     * @param key               the key
     * @param value             the value inserted if the pair was not present
     * @param remappingFunction the function used to update an existing pair value
     * @param ttl               the time-to-live (after which it will expire) in milliseconds
     * @return the updated value
     */
    @Nullable V merge(final @Nullable K key, final @NotNull V value,
                      final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction,
                      final long ttl);

    /**
     * Updates an element in the map.
     * The element will maintain its expiration time.
     *
     * @param key               the key
     * @param value             the value inserted if the pair was not present
     * @param remappingFunction the function used to update an existing pair value
     * @return the updated value
     */
    @Override
    @Nullable V merge(final @Nullable K key, final @NotNull V value, final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction);

    /**
     * Adds all the elements of the given map to the current one.
     *
     * @param map the map
     */
    void putAll(final @NotNull ExpiringMap<? extends K, ? extends V> map);

    /**
     * Adds all the elements of the given map to the current one.
     * Each element will have the same expiration time.
     *
     * @param map the map to take elements from
     * @param ttl the time-to-live (after which the elements will expire)
     */
    void putAll(final @NotNull Map<? extends K, ? extends V> map, final @NotNull Duration ttl);

    /**
     * Adds all the elements of the given map to the current one.
     * Each element will have the same expiration time.
     *
     * @param map the map to take elements from
     * @param ttl the time-to-live (after which the elements will expire) in milliseconds
     */
    void putAll(final @NotNull Map<? extends K, ? extends V> map, final long ttl);

    /**
     * Adds all the elements of the given map to the current one.
     * If the map is not a {@link ExpiringMap}, every element will have no expiration time.
     *
     * @param map the map to take elements from
     */
    @Override
    void putAll(final @NotNull Map<? extends K, ? extends V> map);

    /**
     * Gets the remaining time-to-live of the element with the given key.
     *
     * @param key the key
     * @return the TTL ({@code null} if not present)
     */
    @Nullable Duration getTtl(final @Nullable K key);

    /**
     * Updates the time-to-live of the given element.
     * Requires the key to be present and not expired.
     *
     * @param key the key of the element
     * @param ttl the new time-to-live
     */
    void renew(final @Nullable K key, final @NotNull Duration ttl);

    /**
     * Updates the time-to-live of the given element.
     * Requires the key to be present and not expired.
     *
     * @param key the key of the element
     * @param ttl the new time-to-live in milliseconds
     */
    void renew(final @Nullable K key, final long ttl);

    /**
     * Prints out the contents of this map.
     * <br>
     * Entries will be printed in the format {@code <key>=<value>}.
     * <br>
     * If an entry is <b>never expiring</b>, it will be printed as {@code <key>=<value> (!)}.
     * <br>
     * If an entry is <b>expired</b> but not yet removed, it will be printed as {@code <key>=<value> (*)}.
     *
     * @return the string representation of this map
     */
    @Override
    @NotNull String toString();

    /**
     * Initializes a new passive ExpirationMap.
     * <br>
     * The key-value pairs will persist in memory <b>FOREVER</b>,
     * until the user <b>manually</b> removes them.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     * @return the map
     */
    static <K, V> @NotNull ExpiringMap<K, V> passive() {
        return new PassiveExpiringMap<>();
    }

    /**
     * Initializes a new lazy ExpirationMap.
     * <br>
     * The key-value pairs will persist in memory until an operation has been done
     * to the map itself, at which point the expired elements will be removed.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     * @return the map
     */
    static <K, V> @NotNull ExpiringMap<K, V> lazy() {
        return new LazyExpiringMap<>();
    }

    /**
     * Initializes a new scheduled ExpirationMap.
     * <br>
     * The key-value pairs will be periodically check for expiration and be removed.
     *
     * @param <K>          the type of the keys
     * @param <V>          the type of the values
     * @param scheduler    the scheduler that will handle the periodical removal
     * @param taskInterval the interval upon which to check expirations
     * @return the map
     */
    static <K, V> @NotNull ExpiringMap<K, V> scheduled(final @NotNull ScheduledExecutorService scheduler,
                                                       final @NotNull Duration taskInterval) {
        return new ScheduledExpiringMap<>(scheduler, taskInterval);
    }

}
