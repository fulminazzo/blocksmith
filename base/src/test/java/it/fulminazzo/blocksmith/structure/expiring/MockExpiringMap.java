package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A no-op implementation of {@link AbstractExpiringMap} that never checks for expiration.
 * Intended for testing purposes only.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public class MockExpiringMap<K, V> extends AbstractExpiringMap<K, V> {

    @Override
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable Object key) {
        return delegate.get(key);
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

}
