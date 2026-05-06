package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExpiringMap} with lazy unloading.
 * Entries will be kept in memory until an operation occurs on the map,
 * at which point they will be checked for expiration.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
final class LazyExpiringMap<K, V> extends AbstractExpiringMap<K, V> {

    @Override
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable Object key) {
        ExpiringEntry<V> entry = delegate.get(key);
        if (entry != null && entry.isExpired()) {
            delegate.remove(key);
            return null;
        } else return entry;
    }

    @Override
    public int size() {
        clearExpired();
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        clearExpired();
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return getExpiring(key) != null;
    }

    @Override
    public V get(final Object key) {
        ExpiringEntry<V> entry = getExpiring(key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public V remove(final Object key) {
        ExpiringEntry<V> entry = getExpiring(key);
        if (entry == null) return null;
        else {
            delegate.remove(key);
            return entry.getValue();
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        clearExpired();
        return new HashSet<>(delegate.keySet());
    }

    @Override
    public @NotNull Collection<V> values() {
        clearExpired();
        return delegate.values().stream().map(ExpiringEntry::getValue).collect(Collectors.toList());
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        clearExpired();
        return super.entrySet();
    }

}
