package it.fulminazzo.blocksmith.structure.expiring;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * Basic implementation of {@link ExpiringList} with common logic.
 *
 * @param <E> the type of the elements
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class AbstractExpiringList<E> extends AbstractExpiringCollection<E> implements ExpiringList<E> {
    protected final @NotNull List<ExpiringEntry<E>> delegate = Collections.synchronizedList(new ArrayList<>());

    /**
     * Gets the expiring entry associated with an index.
     *
     * @param index the index
     * @return the expiring entry (or {@code null} if not found)
     */
    protected @Nullable ExpiringEntry<E> getExpiring(final int index) {
        return delegate.get(index);
    }

    /**
     * Manually removes all the expired entries.
     */
    public void clearExpired() {
        delegate.removeIf(ExpiringEntry::isExpired);
    }

    @Override
    @NotNull Collection<ExpiringEntry<E>> expiringEntries() {
        return delegate;
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
        if (collection instanceof ExpiringCollection<?>) return addAll((ExpiringCollection<? extends E>) collection);
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

}
