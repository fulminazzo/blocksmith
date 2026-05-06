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

    @Override
    public void add(final int index, final @Nullable E element, final @NotNull Duration ttl) {
        add(index, element, ttl.toMillis());
    }

    @Override
    public void add(final int index, final @Nullable E element) {
        add(index, element, ExpiringEntry.NEVER_EXPIRE);
    }

    @Override
    public boolean addAll(final int index, final @NotNull ExpiringList<? extends E> list) {
        return addAllHelper(index, list);
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
