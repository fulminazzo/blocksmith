package it.fulminazzo.blocksmith.structure.expiring;

import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExpiringSet} that delegates to a {@link ExpiringMap} the caching logic.
 *
 * @param <E> the type of the elements
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DelegateExpiringSet<E> extends AbstractExpiringCollection<E> implements ExpiringSet<E> {
    private static final @NotNull Object PRESENT = new Object();
    private final AbstractExpiringMap<E, Object> delegate;

    @Override
    public boolean add(final @Nullable E element, final long ttl) {
        return delegate.put(element, PRESENT, ttl) == null;
    }

    @Override
    public boolean add(final @Nullable E element) {
        return delegate.put(element, PRESENT) == null;
    }

    @Override
    public boolean remove(final @Nullable Object object) {
        return delegate.remove(object) != null;
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        return delegate.keySet().containsAll(collection);
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable E element) {
        return delegate.getTtl(element);
    }

    @Override
    public void clear() {
        delegate.clear();
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
    public boolean contains(final @Nullable Object object) {
        return delegate.containsKey(object);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return delegate.keySet().iterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return delegate.keySet().toArray();
    }

    @Override
    public @NotNull <T> T @NotNull [] toArray(final @NotNull T @NotNull [] ts) {
        return delegate.keySet().toArray(ts);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        else if (!(obj instanceof Set)) return false;
        Set<?> set = (Set<?>) obj;
        if (set.size() != size()) return false;
        return containsAll(set);
    }

    @Override
    public int hashCode() {
        return delegate.keySet().hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull Collection<ExpiringEntry<E>> expiringEntries() {
        return delegate.delegate.entrySet().stream()
                .map(e ->
                        (ExpiringEntry<E>) Reflect.on(new ExpiringEntry<>(e.getKey(), 1))
                                .set("expireTime", e.getValue().getExpireTime())
                                .get()
                )
                .collect(Collectors.toList());
    }

}
