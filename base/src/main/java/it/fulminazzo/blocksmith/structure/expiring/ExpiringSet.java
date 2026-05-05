package it.fulminazzo.blocksmith.structure.expiring;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An expiring set is a special {@link Set} whose elements are subject to expiration.
 * Each can be defined with a time-to-live (TTL) period after which they will not be present anymore.
 *
 * @param <E> the type of the elements
 */
public interface ExpiringSet<E> extends Set<E>, ExpiringCollection<E> {

    /**
     * Initializes a new passive ExpiringSet.
     * <br>
     * The elements will persist in memory <b>FOREVER</b>,
     * until the user <b>manually</b> removes them.
     *
     * @param <E> the type of the elements
     * @return the set
     */
    static <E> @NotNull ExpiringSet<E> passive() {
        return new DelegateExpiringSet<>((AbstractExpiringMap<E, Object>) ExpiringMap.<E, Object>passive());
    }

    /**
     * Initializes a new lazy ExpiringSet.
     * <br>
     * The elements will persist in memory <b>FOREVER</b>,
     * until the user <b>manually</b> removes them.
     *
     * @param <E> the type of the elements
     * @return the set
     */
    static <E> @NotNull ExpiringSet<E> lazy() {
        return new DelegateExpiringSet<>((AbstractExpiringMap<E, Object>) ExpiringMap.<E, Object>lazy());
    }

    /**
     * Initializes a new scheduled ExpiringSet.
     * <br>
     * The elements will be periodically check for expiration and be removed.
     *
     * @param scheduler    the scheduler that will handle the periodical removal
     * @param taskInterval the interval upon which to check expirations
     * @param <E>          the type of the elements
     * @return the set
     */
    static <E> @NotNull ExpiringSet<E> scheduled(final @NotNull ScheduledExecutorService scheduler,
                                                 final @NotNull Duration taskInterval) {
        return new DelegateExpiringSet<>((AbstractExpiringMap<E, Object>) ExpiringMap.<E, Object>scheduled(
                scheduler,
                taskInterval
        ));
    }

}
