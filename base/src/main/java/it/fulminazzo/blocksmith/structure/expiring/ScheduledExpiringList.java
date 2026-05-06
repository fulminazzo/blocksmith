package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ExpiringList} with scheduled removal for expired elements.
 *
 * @param <E> the type of the elements
 */
final class ScheduledExpiringList<E> extends AbstractExpiringList<E> {

    /**
     * Instantiates a new Scheduled expiring list.
     *
     * @param scheduler    the scheduler to run the internal clearing task
     * @param taskInterval how much time to wait before attempting to clear the cache
     */
    public ScheduledExpiringList(final @NotNull ScheduledExecutorService scheduler,
                                 final @NotNull Duration taskInterval) {
        long millis = taskInterval.toMillis();
        if (millis <= 0)
            throw new IllegalArgumentException("task interval must be greater than 0");
        scheduler.scheduleAtFixedRate(this::clearExpired, millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    @NotNull ExpiringEntry<E> getExpiring(final int index) {
        ExpiringEntry<E> entry = delegate.get(index);
        if (entry.isExpired()) {
            delegate.remove(index);
            return getExpiring(index);
        } else return entry;
    }

    @Override
    public void add(final int index, final @Nullable E element, final long ttl) {
        delegate.add(index, new ExpiringEntry<>(element, ttl));
    }

    @Override
    public E set(final int index, final @Nullable E element, final long ttl) {
        return delegate.set(index, new ExpiringEntry<>(element, ttl)).getValue();
    }

    @Override
    public E get(final int index) {
        return getExpiring(index).getValue();
    }

    @Override
    public E remove(final int index) {
        ExpiringEntry<E> entry = getExpiring(index);
        delegate.remove(entry);
        return entry.getValue();
    }

    @Override
    public @NotNull ExpiringList<E> subList(final int fromIndex, final int toIndex) {
        LazyExpiringList<E> sub = new LazyExpiringList<>();
        sub.delegate.addAll(delegate.subList(fromIndex, toIndex));
        return sub;
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
    public boolean contains(final Object o) {
        return delegate.stream().anyMatch(e -> Objects.equals(e.getValue(), o));
    }

    @Override
    public boolean remove(final Object o) {
        return delegate.removeIf(e -> Objects.equals(e.getValue(), o));
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        return collection.stream().allMatch(e1 ->
                delegate.stream().anyMatch(e2 ->
                        Objects.equals(e2.getValue(), e1)
                ));
    }

}
