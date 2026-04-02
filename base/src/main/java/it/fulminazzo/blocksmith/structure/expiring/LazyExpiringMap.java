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
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable K key) {
        clearExpired();
        return delegate.get(key);
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
        clearExpired();
        return delegate.containsKey(key);
    }

    @Override
    public V get(final Object key) {
        clearExpired();
        ExpiringEntry<V> entry = delegate.get(key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public V remove(final Object key) {
        clearExpired();
        ExpiringEntry<V> entry = delegate.remove(key);
        return entry == null ? null : entry.getValue();
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

    private void clearExpired() {
        for (Entry<K, ExpiringEntry<V>> entry : new ArrayList<>(delegate.entrySet()))
            if (entry.getValue().isExpired())
                delegate.remove(entry.getKey());
    }

}
