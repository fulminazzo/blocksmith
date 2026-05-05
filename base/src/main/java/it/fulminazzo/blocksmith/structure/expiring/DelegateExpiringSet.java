package it.fulminazzo.blocksmith.structure.expiring;

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
final class DelegateExpiringSet<E> implements ExpiringSet<E> {
    private static final @NotNull Object PRESENT = new Object();
    private final AbstractExpiringMap<E, Object> delegate;

    @Override
    public boolean add(final @Nullable E element, final @NotNull Duration ttl) {
        return add(element, ttl.toMillis());
    }

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
    public boolean addAll(final @NotNull ExpiringSet<? extends E> set) {
        return addAllHelper(set);
    }

    private <E1 extends E> boolean addAllHelper(final @NotNull ExpiringSet<E1> set) {
        boolean added = false;
        for (E1 e : set) {
            Duration ttl = set.getTtl(e);
            if (ttl != null) added |= add(e, ttl);
        }
        return added;
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends E> collection, final @NotNull Duration ttl) {
        return addAll(collection, ttl.toMillis());
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends E> collection, final long ttl) {
        boolean added = false;
        for (E e : collection) added |= add(e, ttl);
        return added;
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends E> collection) {
        if (collection instanceof ExpiringSet<?>) return addAll((ExpiringSet<? extends E>) collection);
        else {
            boolean added = false;
            for (E e : collection) added |= add(e);
            return added;
        }
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> collection) {
        boolean modified = false;
        for (E element : this)
            if (!collection.contains(element)) {
                remove(element);
                modified = true;
            }
        return modified;
    }

    @Override
    public boolean removeAll(final @NotNull Collection<?> collection) {
        boolean removed = false;
        for (Object o : collection) removed |= remove(o);
        return removed;
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable E key) {
        return delegate.getTtl(key);
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

    @Override
    public @NotNull String toString() {
        return String.format("[%s]", delegate.delegate.entrySet().stream()
                .map(e -> {
                    E key = e.getKey();
                    ExpiringEntry<?> value = e.getValue();
                    StringBuilder builder = new StringBuilder(key.toString());
                    if (value.neverExpires())
                        builder.append(" " + ExpiringEntry.NEVER_EXPIRING_CHAR);
                    else if (value.isExpired()) builder.append(" " + ExpiringEntry.EXPIRED_CHAR);
                    return builder.toString();
                })
                .collect(Collectors.joining(", ")));
    }

}
