package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An expiring map is a special {@link Map} whose elements are subject to expiration.
 * Each must be defined with a time-to-live (TTL) period after which they will not be
 * present anymore.
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
     *
     * @param key   the key
     * @param value the value
     * @return the previous value present in the map
     * @deprecated <code>put</code> methods with TTL parameter should be used instead
     */
    @Override
    @Deprecated
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
     *
     * @param key   the key
     * @param value the value
     * @return the value of the map if present, otherwise the value parameter
     * @deprecated <code>putIfAbsent</code> methods with TTL parameter should be used instead
     */
    @Deprecated
    @Override
    @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value);

    /**
     * If a pair with the given key and old value is present, it is replaced with the new value.
     *
     * @param key      the key
     * @param oldValue the old value
     * @param newValue the new value
     * @param ttl      the time-to-live (after which it will expire)
     * @return <code>true</code> if the replacement was successful,
     * <code>false</code> if no element matching the pair was found
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
     * @return <code>true</code> if the replacement was successful,
     * <code>false</code> if no element matching the pair was found
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
     * @return <code>true</code> if the replacement was successful,
     * <code>false</code> if no element matching the pair was found
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
     *
     * @param key             the key
     * @param mappingFunction the function to get the new value from
     * @return the value of the map if present, otherwise the value parameter
     * @deprecated <code>computeIfAbsent</code> methods with TTL parameter should be used instead
     */
    @Deprecated
    @Override
    @Nullable V computeIfAbsent(final @Nullable K key, final @NotNull Function<? super K, ? extends V> mappingFunction);

    /**
     * Updates an element in the map with a new value computed from the given function.
     * The time-to-live is <b>not</b> refreshed.
     *
     * @param key               the key
     * @param remappingFunction the function to get the new value from
     * @return the value of the map if present, otherwise <code>null</code>
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
     *
     * @param key               the key
     * @param remappingFunction the function to update the value from
     * @return the previous value present in the map
     * @deprecated <code>compute</code> methods with TTL parameter should be used instead
     */
    @Deprecated
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
     *
     * @param key               the key
     * @param value             the value inserted if the pair was not present
     * @param remappingFunction the function used to update an existing pair value
     * @return the updated value
     * @deprecated <code>merge</code> methods with TTL parameter should be used instead
     */
    @Deprecated
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
     * Each element will have the same expiration time.
     *
     * @param map the map to take elements from
     * @deprecated <code>putAll</code> methods with TTL parameter should be used instead
     */
    @Override
    @Deprecated
    void putAll(final @NotNull Map<? extends K, ? extends V> map);

    /**
     * Gets the remaining time-to-live of the element with the given key.
     *
     * @param key the key
     * @return the TTL (<code>null</code> if not present)
     */
    @Nullable Duration getTtl(final @Nullable K key);

    /**
     * Updates the time-to-live of the given element.
     * Requires the key to be present and not expired.
     *
     * @param key the key of the element
     * @param ttl the new time-to-live
     */
    void renewTtl(final @Nullable K key, final @NotNull Duration ttl);

    /**
     * Updates the time-to-live of the given element.
     * Requires the key to be present and not expired.
     *
     * @param key the key of the element
     * @param ttl the new time-to-live in milliseconds
     */
    void renewTtl(final @Nullable K key, final long ttl);

}
