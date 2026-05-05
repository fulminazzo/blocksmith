package it.fulminazzo.blocksmith.structure.expiring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies the entry of a Map with an expiration time.
 *
 * @param <V> the type of the value
 */
@Data
final class ExpiringEntry<V> {
    static final String NEVER_EXPIRING_CHAR = "(!)";
    static final String EXPIRED_CHAR = "(*)";

    private V value;
    @EqualsAndHashCode.Exclude
    private long expireTime;

    /**
     * Instantiates a new Expiring entry.
     *
     * @param value the value
     * @param ttl   the time-to-live (after which it will expire)
     */
    public ExpiringEntry(final V value, final long ttl) {
        this.value = value;
        setTimeToLive(ttl);
    }

    /**
     * Checks if the current entry never expires.
     *
     * @return {@code true} if it does not
     */
    public boolean neverExpires() {
        return expireTime == AbstractExpiringMap.NEVER_EXPIRE;
    }

    /**
     * Checks if the current entry is expired.
     *
     * @return {@code true} if it is
     */
    public boolean isExpired() {
        return expireTime <= AbstractExpiringMap.now();
    }

    /**
     * Updates the time-to-live.
     *
     * @param ttl the time-to-live (after which it will expire)
     */
    public void setTimeToLive(final long ttl) {
        AbstractExpiringMap.checkTtl(ttl);
        this.expireTime = ttl == AbstractExpiringMap.NEVER_EXPIRE ? AbstractExpiringMap.NEVER_EXPIRE : AbstractExpiringMap.now() + ttl;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder(value == null ? "null" : value.toString());
        if (neverExpires()) builder.append(" " + NEVER_EXPIRING_CHAR);
        else if (isExpired()) builder.append(" " + EXPIRED_CHAR);
        return builder.toString();
    }

}
