package it.fulminazzo.blocksmith.structure.expiring;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Basic implementation of {@link ExpiringMap} with internal logic.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public abstract class AbstractExpiringMap<K, V> implements ExpiringMap<K, V> {
    protected final @NotNull Map<K, ExpiringEntry<V>> delegate = new ConcurrentHashMap<>();

    /**
     * Gets the expiring entry associated with a key.
     *
     * @param key the key
     * @return the expiring entry (or <code>null</code> if not found)
     */
    protected abstract @Nullable ExpiringEntry<V> getExpiring(final @Nullable K key);

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value, final @NotNull Duration ttl) {
        return put(key, value, ttl.toMillis());
    }

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value) {
        throw getPutNotSupportedException();
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
        throw getPutNotSupportedException();
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
        throw getPutNotSupportedException();
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
        throw getPutNotSupportedException();
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
        throw getPutNotSupportedException();
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
    public void putAll(final @NotNull Map<? extends K, ? extends V> map) {
        throw getPutNotSupportedException();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable K key) {
        ExpiringEntry<V> entry = getExpiring(key);
        return entry == null ? null : Duration.ofMillis(entry.getExpireTime() - now());
    }

    @Override
    public void renewTtl(final @Nullable K key, final @NotNull Duration ttl) {
        renewTtl(key, ttl.toMillis());
    }

    @Override
    public void renewTtl(final @Nullable K key, final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) throw new NoSuchElementException("key: " + key);
        entry.setTimeToLive(ttl);
    }

    private @NotNull UnsupportedOperationException getPutNotSupportedException() {
        return new UnsupportedOperationException(getClass().getSimpleName() + " does not support put without TTL");
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
            this.expireTime = now() + ttl;
        }

    }

}
