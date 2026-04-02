package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A special implementation of {@link Map} supporting expiration dates for saved data.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public class ExpirationMap<K, V> implements Map<K, V> {
    private final @NotNull Map<K, TimedValue<V>> internalMap = new ConcurrentHashMap<>();

    private long expiryInMillis;

    /**
     * Instantiates a new Expiration map.
     *
     * @param expiryInMillis the expiration time in milliseconds
     */
    public ExpirationMap(final long expiryInMillis) {
        setExpiry(expiryInMillis);
    }

    /**
     * Instantiates a new Expiration map.
     *
     * @param map            the data to use to populate this map
     * @param expiryInMillis the expiration time in milliseconds
     */
    public ExpirationMap(final @NotNull Map<K, V> map, final long expiryInMillis) {
        setExpiry(expiryInMillis);
        putAll(map);
    }

    /**
     * Sets the expiration time when storing a key-value pair.
     *
     * @param expiryInMillis the expiration time (in milliseconds)
     */
    public void setExpiry(final @PositiveOrZero(exceptionMessage = "the expiration time must be positive or zero") long expiryInMillis) {
        Validator.validateMethod(expiryInMillis);
        this.expiryInMillis = expiryInMillis;
    }

    @Override
    public int size() {
        cleanup();
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(final Object value) {
        cleanup();
        return internalMap.values().stream().anyMatch(v -> Objects.equals(v.getValue(), value));
    }

    @Override
    public V get(final Object key) {
        TimedValue<V> value = internalMap.get(key);
        if (value != null) {
            if (value.isExpired()) internalMap.remove(key);
            else return value.getValue();
        }
        return null;
    }

    @Override
    public @Nullable V put(final K key, final V value) {
        V previous = get(key);
        internalMap.put(key, new TimedValue<>(value, expiryInMillis));
        return previous;
    }

    @Override
    public V remove(final Object key) {
        V previous = get(key);
        internalMap.remove(key);
        return previous;
    }

    @Override
    public void putAll(final @NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public @NotNull Set<K> keySet() {
        cleanup();
        return internalMap.keySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        cleanup();
        return internalMap.values().stream().map(TimedValue::getValue).collect(Collectors.toList());
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        cleanup();
        return internalMap.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toSet());
    }

    private void cleanup() {
        for (K key : new ArrayList<>(internalMap.keySet())) {
            TimedValue<V> value = internalMap.get(key);
            if (value.isExpired()) internalMap.remove(key);
        }
    }

    private static final class TimedValue<V> {
        @Getter
        private final V value;
        private final long expirationTime;

        /**
         * Instantiates a new Timed value.
         *
         * @param value          the value
         * @param expiryInMillis the expiry in millis
         */
        public TimedValue(final V value, final long expiryInMillis) {
            this.value = value;
            this.expirationTime = expiryInMillis > 0
                    ? System.currentTimeMillis() + expiryInMillis
                    : Long.MAX_VALUE;
        }

        /**
         * Checks if the value is expired.
         *
         * @return <code>true</code> if it is
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

    }

}
