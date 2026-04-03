package it.fulminazzo.blocksmith.structure.expiring;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
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
        long millis = taskInterval.toMillis();
        if (millis <= 0)
            throw new IllegalArgumentException("task interval must be greater than 0");
        scheduler.scheduleAtFixedRate(this::clearExpired, millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    protected @Nullable ExpiringEntry<V> getExpiring(final @Nullable Object key) {
        ExpiringEntry<V> entry = delegate.get(key);
        if (entry == null) return null;
        else if (entry.isExpired()) {
            delegate.remove(key);
            return null;
        } else return entry;
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
        return entry == null || entry.isExpired() ? null : entry.getValue();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return delegate.entrySet().stream()
                .filter(e -> !e.getValue().isExpired())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return delegate.values().stream()
                .filter(e -> !e.isExpired())
                .map(ExpiringEntry::getValue)
                .collect(Collectors.toList());
    }

}
