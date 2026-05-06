package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A no-op implementation of {@link AbstractExpiringCollection} that never checks for expiration.
 * Intended for testing purposes only.
 *
 * @param <E> the type of the elements
 */
public class MockExpiringCollection<E> extends AbstractExpiringCollection<E> {
    private final @NotNull Collection<ExpiringEntry<E>> delegate = new ArrayList<>();

    @Override
    @NotNull Collection<ExpiringEntry<E>> expiringEntries() {
        return new HashSet<>(delegate);
    }

    @Override
    public boolean add(final @Nullable E element, final long ttl) {
        return delegate.add(new ExpiringEntry<>(element, ttl));
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
    public boolean contains(Object o) {
        return actualCollection().contains(o);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return actualCollection().iterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return actualCollection().toArray();
    }

    @Override
    public @NotNull <T> T @NotNull [] toArray(final @NotNull T @NotNull [] ts) {
        return actualCollection().toArray(ts);
    }

    @Override
    public boolean add(final @Nullable E element) {
        return delegate.add(new ExpiringEntry<>(element, ExpiringEntry.NEVER_EXPIRE));
    }

    @Override
    public boolean remove(Object o) {
        return delegate.removeIf(t -> Objects.equals(t.getValue(), o));
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        return actualCollection().containsAll(collection);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable E element) {
        return delegate.stream()
                .filter(e -> Objects.equals(e.getValue(), element))
                .map(e -> e.getExpireTime() - System.currentTimeMillis())
                .filter(t -> t > 0)
                .map(Duration::ofMillis)
                .findAny().orElse(null);
    }

    private @NotNull Collection<E> actualCollection() {
        return delegate.stream().map(ExpiringEntry::getValue).collect(Collectors.toList());
    }

}
