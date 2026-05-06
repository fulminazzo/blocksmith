package it.fulminazzo.blocksmith.structure.expiring;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link ExpiringList} with common logic.
 *
 * @param <E> the type of the elements
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class AbstractExpiringList<E> extends AbstractExpiringCollection<E> implements ExpiringList<E> {
    protected final @NotNull List<@NotNull ExpiringEntry<E>> delegate = Collections.synchronizedList(new ArrayList<>());

    /**
     * Gets the expiring entry associated with an index.
     *
     * @param index the index
     * @return the expiring entry
     * @throws IndexOutOfBoundsException if either the index is out of range OR
     * the entry at the given index expired and there is no replacement entry
     */
    abstract @NotNull ExpiringEntry<E> getExpiring(final int index);

    /**
     * Manually removes all the expired entries.
     */
    public void clearExpired() {
        delegate.removeIf(ExpiringEntry::isExpired);
    }

    @Override
    public @Nullable Duration getTtl(final @Nullable E element) {
        int index = indexOf(element);
        ExpiringEntry<E> entry = index == -1 ? null : getExpiring(index);
        return entry == null ? null : Duration.ofMillis(entry.getExpireTime() - now());
    }

    @Override
    @NotNull Collection<ExpiringEntry<E>> expiringEntries() {
        return delegate;
    }

    @Override
    public boolean add(final @Nullable E element, final long ttl) {
        return delegate.add(new ExpiringEntry<>(element, ttl));
    }

    @Override
    public void add(final int index, final @Nullable E element, final @NotNull Duration ttl) {
        add(index, element, ttl.toMillis());
    }

    @Override
    public void add(final int index, final @Nullable E element) {
        add(index, element, ExpiringEntry.NEVER_EXPIRE);
    }

    @Override
    public boolean addAll(final int index, final @NotNull ExpiringCollection<? extends E> collection) {
        return addAllHelper(index, collection);
    }

    private <E1 extends E> boolean addAllHelper(int index, final @NotNull ExpiringCollection<E1> collection) {
        boolean added = false;
        for (E1 e : collection) {
            Duration ttl = collection.getTtl(e);
            if (ttl != null) {
                add(index++, e, ttl);
                added = true;
            }
        }
        return added;
    }

    @Override
    public boolean addAll(final int index, final @NotNull Collection<? extends E> collection, final @NotNull Duration ttl) {
        return addAll(index, collection, ttl.toMillis());
    }

    @Override
    public boolean addAll(int index, final @NotNull Collection<? extends E> collection, final long ttl) {
        boolean added = false;
        for (E e : collection) {
            add(index++, e, ttl);
            added = true;
        }
        return added;
    }

    @Override
    public boolean addAll(int index, final @NotNull Collection<? extends E> collection) {
        if (collection instanceof ExpiringCollection<?>) return addAll(index, (ExpiringCollection<? extends E>) collection);
        else {
            boolean added = false;
            for (E e : collection) {
                add(index++, e);
                added = true;
            }
            return added;
        }
    }

    @Override
    public E set(final int index, final @Nullable E element, final @NotNull Duration ttl) {
        return set(index, element, ttl.toMillis());
    }

    @Override
    public E set(final int index, final @Nullable E element) {
        return set(index, element, ExpiringEntry.NEVER_EXPIRE);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(o)) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        for (int i = size() - 1; i >= 0; i--) {
            if (get(i).equals(o)) return i;
        }
        return -1;
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public @NotNull ListIterator<E> listIterator(final int index) {
        clearExpired();
        return delegate.subList(index, size()).stream()
                .map(ExpiringEntry::getValue)
                .collect(Collectors.toList())
                .listIterator();
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return stream().toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T @NotNull [] toArray(final T[] ts) {
        Object[] elements = toArray();
        if (ts.length < elements.length)
            return (T[]) Arrays.copyOf(elements, elements.length, ts.getClass());
        System.arraycopy(elements, 0, ts, 0, elements.length);
        if (ts.length > elements.length)
            ts[elements.length] = null;
        return ts;
    }

    /**
     * Gets the current time of the system.
     *
     * @return the time in milliseconds
     */
    protected static long now() {
        return System.currentTimeMillis();
    }

}
