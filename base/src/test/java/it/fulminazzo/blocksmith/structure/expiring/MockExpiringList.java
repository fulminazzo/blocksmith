package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A no-op implementation of {@link AbstractExpiringList} that never checks for expiration.
 * Intended for testing purposes only.
 *
 * @param <E> the type of the elements
 */
public class MockExpiringList<E> extends AbstractExpiringList<E> {

    @Override
    @NotNull ExpiringEntry<E> getExpiring(final int index) {
        return delegate.get(index);
    }

    @Override
    public void add(final int index, final @Nullable E element, final long ttl) {
        delegate.add(index, new ExpiringEntry<>(element, ttl));
    }

    @Override
    public E set(final int index, final @Nullable E element, final long ttl) {
        ExpiringEntry<E> entry = delegate.set(index, new ExpiringEntry<>(element, ttl));
        return entry.getValue();
    }

    @Override
    public E get(final int index) {
        ExpiringEntry<E> entry = delegate.get(index);
        return entry.getValue();
    }

    @Override
    public E remove(final int i) {
        ExpiringEntry<E> entry = delegate.remove(i);
        return entry.getValue();
    }

    @Override
    public @NotNull ExpiringList<E> subList(final int fromIndex, final int toIndex) {
        MockExpiringList<E> newList = new MockExpiringList<>();
        newList.delegate.addAll(delegate.subList(fromIndex, toIndex));
        return newList;
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
        return delegate.stream().anyMatch(e -> e.getValue().equals(o));
    }

    @Override
    public boolean remove(Object o) {
        return delegate.removeIf(e -> e.getValue().equals(o));
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        return delegate.stream().allMatch(e -> collection.contains(e.getValue()));
    }

}
