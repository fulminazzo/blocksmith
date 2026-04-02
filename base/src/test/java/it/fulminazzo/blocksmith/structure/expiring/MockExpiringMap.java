package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A no-op implementation of {@link AbstractExpiringMap} that never checks for expiration.
 * Intended for testing purposes only.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public final class MockExpiringMap<K, V> extends AbstractExpiringMap<K, V> {

    @Override
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable K key) {
        return delegate.get(key);
    }

    @Override
    public @Nullable V put(final @Nullable K key, final @Nullable V value, final long ttl) {
        checkTtl(ttl);
        ExpiringEntry<V> previous = delegate.put(key, new ExpiringEntry<>(value, ttl));
        return previous == null ? null : previous.getValue();
    }

    @Override
    public void putAll(final @NotNull Map<? extends K, ? extends V> map, final long ttl) {
        checkTtl(ttl);
        map.forEach((k, v) -> put(k, v, ttl));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(final @Nullable Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final @Nullable Object value) {
        return delegate.values().stream()
                .anyMatch(entry -> entry.getValue() != null && entry.getValue().equals(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(final @Nullable Object key) {
        ExpiringEntry<V> entry = getExpiring((K) key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public @Nullable V remove(final @Nullable Object key) {
        ExpiringEntry<V> removed = delegate.remove(key);
        return removed == null ? null : removed.getValue();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        return delegate.values().stream()
                .map(ExpiringEntry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return delegate.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toSet());
    }

}
