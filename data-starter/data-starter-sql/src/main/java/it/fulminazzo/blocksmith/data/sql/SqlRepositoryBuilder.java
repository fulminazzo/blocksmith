package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

/**
 * A builder for {@link SqlRepository}.
 */
public final class SqlRepositoryBuilder {
    private static final int maximumPoolSize = 20;
    private static final int minimumIdle = 5;
    private static final long connectionTimeout = 30 * 1000;
    private static final long idleTimeout = 10 * 60 * 1000;
    private static final long maxLifeTime = 30 * 60 * 1000;

    private final @NotNull HikariConfig config;

    /**
     * Instantiates a new SQL repository builder.
     */
    SqlRepositoryBuilder() {
        this.config = new HikariConfig();
        maximumPoolSize(maximumPoolSize)
                .minimumIdle(minimumIdle)
                .connectionTimeout(connectionTimeout)
                .idleTimeout(idleTimeout)
                .maxLifeTime(maxLifeTime);
    }

    /**
     * Sets the maximum number of concurrent connections.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositoryBuilder maximumPoolSize(final int maximumPoolSize) {
        config.setMaximumPoolSize(maximumPoolSize);
        return this;
    }

    /**
     * Sets the minimum idle connections.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositoryBuilder minimumIdle(final int minimumIdle) {
        config.setMinimumIdle(minimumIdle);
        return this;
    }

    /**
     * Sets the connections timeout.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositoryBuilder connectionTimeout(final long connectionTimeout) {
        config.setConnectionTimeout(connectionTimeout);
        return this;
    }

    /**
     * Sets the idle timeout for connections.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositoryBuilder idleTimeout(final long idleTimeout) {
        config.setIdleTimeout(idleTimeout);
        return this;
    }

    /**
     * Sets the maximum lifetime of a connection.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositoryBuilder maxLifeTime(final long maxLifetime) {
        config.setMaxLifetime(maxLifetime);
        return this;
    }

}
