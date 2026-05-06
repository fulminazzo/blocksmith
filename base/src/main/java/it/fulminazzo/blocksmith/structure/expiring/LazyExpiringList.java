package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of {@link ExpiringList} with lazy unloading.
 * Entities will be kept in memory until an operation occurs on the list,
 * at which point they will be checked for expiration.
 *
 * @param <E> the type of the elements
 */
final class LazyExpiringList<E> extends AbstractExpiringList<E> {

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
        clearExpired();
        delegate.add(index, new ExpiringEntry<>(element, ttl));
    }

    @Override
    public E set(final int index, final @Nullable E element, final long ttl) {
        clearExpired();
        return delegate.set(index, new ExpiringEntry<>(element, ttl)).getValue();
    }

    @Override
    public E get(final int index) {
        return getExpiring(index).getValue();
    }

    @Override
    public E remove(final int index) {
        ExpiringEntry<E> entry = getExpiring(index);
        delegate.remove(index);
        return entry.getValue();
    }

    @Override
    public @NotNull ExpiringList<E> subList(final int fromIndex, final int toIndex) {
        clearExpired();
        LazyExpiringList<E> sub = new LazyExpiringList<>();
        sub.delegate.addAll(delegate.subList(fromIndex, toIndex));
        return sub;
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
    public boolean contains(final Object o) {
        clearExpired();
        return delegate.stream().anyMatch(e -> Objects.equals(e.getValue(), o));
    }

    @Override
    public boolean remove(final Object o) {
        clearExpired();
        return delegate.removeIf(e -> Objects.equals(e.getValue(), o));
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        clearExpired();
        return collection.stream().allMatch(e1 ->
                delegate.stream().anyMatch(e2 ->
                        Objects.equals(e2.getValue(), e1)
                ));
    }

}
