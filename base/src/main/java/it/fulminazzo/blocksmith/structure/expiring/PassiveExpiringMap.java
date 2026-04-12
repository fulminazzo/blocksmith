package it.fulminazzo.blocksmith.structure.expiring;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExpiringMap} with no removal of the entities.
 * <br>
 * <b>WARNING</b>: removal is delegated to the user.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
@RequiredArgsConstructor
final class PassiveExpiringMap<K, V> extends AbstractExpiringMap<K, V> {

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
        ExpiringEntry<V> entry = delegate.remove(key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return new HashSet<>(delegate.keySet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return delegate.values().stream()
                .map(ExpiringEntry::getValue)
                .collect(Collectors.toList());
    }

}
