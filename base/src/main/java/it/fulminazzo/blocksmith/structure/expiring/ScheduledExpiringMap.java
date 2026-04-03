package it.fulminazzo.blocksmith.structure.expiring;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExpiringMap} with scheduled removal for expired entries.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
@RequiredArgsConstructor
final class ScheduledExpiringMap<K, V> extends AbstractExpiringMap<K, V> {

    /**
     * Instantiates a new Scheduled expiring map.
     *
     * @param scheduler    the scheduler to run the internal clearing task
     * @param taskInterval how much time to wait before attempting to clear the cache
     */
    public ScheduledExpiringMap(final @NotNull ScheduledExecutorService scheduler,
                                final @NotNull Duration taskInterval) {
        scheduler.scheduleAtFixedRate(this::clearExpired, taskInterval.toMillis(), taskInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable K key) {
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
        return delegate.containsKey(key);
    }

    @Override
    public V get(final Object key) {
        ExpiringEntry<V> entry = delegate.get(key);
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
        return delegate.values().stream().map(ExpiringEntry::getValue).collect(Collectors.toList());
    }

}
