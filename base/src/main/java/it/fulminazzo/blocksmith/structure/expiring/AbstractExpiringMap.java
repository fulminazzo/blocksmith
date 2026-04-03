package it.fulminazzo.blocksmith.structure.expiring;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link ExpiringMap} with internal logic.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public abstract class AbstractExpiringMap<K, V> implements ExpiringMap<K, V> {
    /**
     * Expire TTL for an endless entry.
     */
    static final long NEVER_EXPIRE = Long.MAX_VALUE;

    protected final @NotNull Map<K, ExpiringEntry<V>> delegate = new ConcurrentHashMap<>();

    /**
     * Gets the expiring entry associated with a key.
     *
     * @param key the key
     * @return the expiring entry (or <code>null</code> if not found)
     */
    protected abstract @Nullable ExpiringEntry<V> getExpiring(final @Nullable Object key);

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value, final @NotNull Duration ttl) {
        return put(key, value, ttl.toMillis());
    }

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value, final long ttl) {
        checkTtl(ttl);
        V previous = get(key);
        delegate.put(key, new ExpiringEntry<>(value, ttl));
        return previous;
    }

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value) {
        return put(key, value, NEVER_EXPIRE);
    }

    @Override
    public boolean containsValue(final Object value) {
        return values().stream().anyMatch(v -> Objects.equals(v, value));
    }

    @Override
    public @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value, final @NotNull Duration ttl) {
        return putIfAbsent(key, value, ttl.toMillis());
    }

    @Override
    public @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value, final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) {
            delegate.put(key, new ExpiringEntry<>(value, ttl));
            return value;
        } else return entry.getValue();
    }

    @Override
    public @Nullable V putIfAbsent(final @Nullable K key, final @Nullable V value) {
        return putIfAbsent(key, value, NEVER_EXPIRE);
    }

    @Override
    public boolean replace(final @Nullable K key,
                           final @Nullable V oldValue,
                           final @Nullable V newValue,
                           final @NotNull Duration ttl) {
        return replace(key, oldValue, newValue, ttl.toMillis());
    }

    @Override
    public boolean replace(final @Nullable K key,
                           final @Nullable V oldValue,
                           final @Nullable V newValue,
                           final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) return false;
        V currentValue = entry.getValue();
        if (Objects.equals(currentValue, oldValue)) {
            entry.setValue(newValue);
            entry.setTimeToLive(ttl);
            return true;
        } else return false;
    }

    @Override
    public boolean replace(final @Nullable K key, final @Nullable V oldValue, final @Nullable V newValue) {
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) return false;
        V currentValue = entry.getValue();
        if (Objects.equals(currentValue, oldValue)) {
            entry.setValue(newValue);
            return true;
        } else return false;
    }

    @Override
    public @Nullable V replace(final @Nullable K key, final @Nullable V value) {
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry != null) {
            V previous = entry.getValue();
            entry.setValue(value);
            return previous;
        } else return null;
    }

    @Override
    public @Nullable V computeIfAbsent(final @Nullable K key,
                                       final @NotNull Function<? super K, ? extends V> mappingFunction,
                                       final @NotNull Duration ttl) {
        return computeIfAbsent(key, mappingFunction, ttl.toMillis());
    }

    @Override
    public @Nullable V computeIfAbsent(final @Nullable K key,
                                       final @NotNull Function<? super K, ? extends V> mappingFunction,
                                       final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) {
            V newValue = mappingFunction.apply(key);
            delegate.put(key, new ExpiringEntry<>(newValue, ttl));
            return newValue;
        } else return entry.getValue();
    }

    @Override
    public @Nullable V computeIfAbsent(final @Nullable K key,
                                       final @NotNull Function<? super K, ? extends V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction, NEVER_EXPIRE);
    }

    @Override
    public @Nullable V computeIfPresent(final @Nullable K key,
                                        final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry != null) {
            V newValue = remappingFunction.apply(key, entry.getValue());
            entry.setValue(newValue);
            return newValue;
        } else return null;
    }

    @Override
    public @Nullable V compute(final @Nullable K key,
                               final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction,
                               final @NotNull Duration ttl) {
        return compute(key, remappingFunction, ttl.toMillis());
    }

    @Override
    public @Nullable V compute(final @Nullable K key,
                               final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction,
                               final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        V newValue = remappingFunction.apply(key, entry == null ? null : entry.getValue());
        if (newValue == null) delegate.remove(key);
        else delegate.put(key, new ExpiringEntry<>(newValue, ttl));
        return entry == null ? null : entry.getValue();
    }

    @Override
    public @Nullable V compute(final @Nullable K key,
                               final @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Duration ttl = getTtl(key);
        return compute(key, remappingFunction, ttl == null ? NEVER_EXPIRE : ttl.toMillis());
    }

    @Override
    public @Nullable V merge(final @Nullable K key,
                             final @NotNull V value,
                             final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction,
                             final @NotNull Duration ttl) {
        return merge(key, value, remappingFunction, ttl.toMillis());
    }

    @Override
    public @Nullable V merge(final @Nullable K key,
                             final @NotNull V value,
                             final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction,
                             final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        V newValue = entry == null ? value : remappingFunction.apply(entry.getValue(), value);
        if (newValue == null) delegate.remove(key);
        else delegate.put(key, new ExpiringEntry<>(newValue, ttl));
        return newValue;
    }

    @Override
    public @Nullable V merge(final @Nullable K key,
                             final @NotNull V value,
                             final @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Duration ttl = getTtl(key);
        return merge(key, value, remappingFunction, ttl == null ? NEVER_EXPIRE : ttl.toMillis());
    }

    @Override
    public void putAll(final @NotNull ExpiringMap<? extends K, ? extends V> map) {
        putAllHelper(map);
    }

    private <EK extends K, EV extends V> void putAllHelper(final @NotNull ExpiringMap<EK, EV> map) {
        map.forEach((k, v) -> {
            Duration ttl = map.getTtl(k);
            if (ttl != null) put(k, v, ttl);
        });
    }

    @Override
    public void putAll(final @NotNull Map<? extends K, ? extends V> map, final @NotNull Duration ttl) {
        putAll(map, ttl.toMillis());
    }

    @Override
    public void putAll(final @NotNull Map<? extends K, ? extends V> map, final long ttl) {
        checkTtl(ttl);
        map.forEach((k, v) -> delegate.put(k, new ExpiringEntry<>(v, ttl)));
    }

    @Override
    public void putAll(final @NotNull Map<? extends K, ? extends V> map) {
        if (map instanceof ExpiringMap) putAll((ExpiringMap<? extends K, ? extends V>) map);
        else putAll(map, NEVER_EXPIRE);
    }

    /**
     * Manually removes all the expired entries.
     */
    public void clearExpired() {
        for (Entry<K, ExpiringEntry<V>> entry : new ArrayList<>(delegate.entrySet()))
            if (entry.getValue().isExpired())
                delegate.remove(entry.getKey());
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        clearExpired();
        return delegate.entrySet().stream()
                .map(e -> new ExpiringEntryMapEntry<>(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable K key) {
        ExpiringEntry<V> entry = getExpiring(key);
        return entry == null ? null : Duration.ofMillis(entry.getExpireTime() - now());
    }

    @Override
    public void renew(final @Nullable K key, final @NotNull Duration ttl) {
        renew(key, ttl.toMillis());
    }

    @Override
    public void renew(final @Nullable K key, final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) throw new NoSuchElementException("key: " + key);
        entry.setTimeToLive(ttl);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Map)) return false;
        Map<?, ?> map = (Map<?, ?>) obj;
        if (map.size() != size()) return false;
        Set<? extends Entry<?, ?>> mapEntries = map.entrySet();
        return entrySet().stream().allMatch(e -> mapEntries.stream().anyMatch(e::equals));
    }

    @Override
    public int hashCode() {
        return entrySet().stream()
                .mapToInt(Entry::hashCode)
                .sum();
    }

    @Override
    public @NotNull String toString() {
        return delegate.toString();
    }

    /**
     * Checks if the given time-to-live is valid.
     *
     * @param ttl the time-to-live
     */
    protected static void checkTtl(final long ttl) {
        if (ttl <= 0) throw new IllegalArgumentException("time-to-live must be more than 0");
    }

    /**
     * Gets the current time of the system.
     *
     * @return the time in milliseconds
     */
    protected static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Identifies the entry of a Map with an expiration time.
     *
     * @param <V> the type of the value
     */
    @Data
    protected static final class ExpiringEntry<V> {
        private V value;
        @EqualsAndHashCode.Exclude
        private long expireTime;

        /**
         * Instantiates a new Expiring entry.
         *
         * @param value the value
         * @param ttl   the time-to-live (after which it will expire)
         */
        public ExpiringEntry(final V value, final long ttl) {
            this.value = value;
            setTimeToLive(ttl);
        }

        /**
         * Checks if the current entry never expires.
         *
         * @return <code>true</code> if it does not
         */
        public boolean neverExpires() {
            return expireTime == NEVER_EXPIRE;
        }

        /**
         * Checks if the current entry is expired.
         *
         * @return <code>true</code> if it is
         */
        public boolean isExpired() {
            return expireTime <= now();
        }

        /**
         * Updates the time-to-live.
         *
         * @param ttl the time-to-live (after which it will expire)
         */
        public void setTimeToLive(final long ttl) {
            checkTtl(ttl);
            this.expireTime = ttl == NEVER_EXPIRE ? NEVER_EXPIRE : now() + ttl;
        }

        @Override
        public @NotNull String toString() {
            StringBuilder builder = new StringBuilder(value == null ? "null" : value.toString());
            if (neverExpires()) builder.append(" (!)");
            else if (isExpired()) builder.append(" (*)");
            return builder.toString();
        }

    }

    @ToString
    @RequiredArgsConstructor
    private static final class ExpiringEntryMapEntry<K, V> implements Entry<K, V> {
        private final @Nullable K key;
        private final @NotNull ExpiringEntry<V> entry;

        @Override
        public @Nullable K getKey() {
            checkExpired();
            return key;
        }

        @Override
        public @Nullable V getValue() {
            checkExpired();
            return entry.getValue();
        }

        @Override
        public V setValue(final @Nullable V value) {
            checkExpired();
            entry.setValue(value);
            return value;
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            checkExpired();
            if (obj instanceof Entry<?, ?>) {
                Entry<?, ?> e = (Entry<?, ?>) obj;
                return Objects.equals(e.getKey(), key) && Objects.equals(e.getValue(), entry.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            checkExpired();
            return Objects.hash(key, entry.getValue());
        }

        private void checkExpired() {
            if (entry.isExpired())
                throw new IllegalStateException(String.format("Entry %s:%s is expired", key, entry.getValue()));
        }

    }

}
