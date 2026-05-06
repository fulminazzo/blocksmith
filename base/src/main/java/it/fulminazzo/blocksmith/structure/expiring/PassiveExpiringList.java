package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExpiringList} with no removal of the elements.
 * <br>
 * <b>WARNING</b>: removal is delegated to the user.
 *
 * @param <E> the type of the elements
 */
final class PassiveExpiringList<E> extends AbstractExpiringList<E> {

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
        PassiveExpiringList<E> sub = new PassiveExpiringList<>();
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

    @Override
    public @NotNull ListIterator<E> listIterator(final int index) {
        return delegate.subList(index, size()).stream()
                .map(ExpiringEntry::getValue)
                .collect(Collectors.toList())
                .listIterator();
    }

}
