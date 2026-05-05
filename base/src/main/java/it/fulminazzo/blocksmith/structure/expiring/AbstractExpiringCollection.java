package it.fulminazzo.blocksmith.structure.expiring;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link ExpiringCollection} with common logic.
 *
 * @param <E> the type of the elements
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class AbstractExpiringCollection<E> implements ExpiringCollection<E> {

    /**
     * Gets the {@link ExpiringEntry}s of this collection.
     *
     * @return the {@link ExpiringEntry}s
     */
    abstract @NotNull Collection<ExpiringEntry<E>> expiringEntries();

    @Override
    public boolean add(final @Nullable E element, final @NotNull Duration ttl) {
        return add(element, ttl.toMillis());
    }

    @Override
    public boolean addAll(final @NotNull ExpiringCollection<? extends E> collection) {
        return addAllHelper(collection);
    }

    private <E1 extends E> boolean addAllHelper(final @NotNull ExpiringCollection<E1> collection) {
        boolean added = false;
        for (E1 e : collection) {
            Duration ttl = collection.getTtl(e);
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
        if (collection instanceof ExpiringCollection<?>) return addAll((ExpiringCollection<? extends E>) collection);
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
    public @NotNull String toString() {
        return String.format("[%s]", expiringEntries().stream()
                .map(e -> {
                    E value = e.getValue();
                    StringBuilder builder = new StringBuilder(value.toString());
                    if (e.neverExpires())
                        builder.append(" " + ExpiringEntry.NEVER_EXPIRING_CHAR);
                    else if (e.isExpired()) builder.append(" " + ExpiringEntry.EXPIRED_CHAR);
                    return builder.toString();
                })
                .collect(Collectors.joining(", ")));
    }

}
