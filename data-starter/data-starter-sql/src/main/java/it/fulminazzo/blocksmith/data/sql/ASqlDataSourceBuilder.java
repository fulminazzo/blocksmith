package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.fulminazzo.blocksmith.data.util.ValidationUtils;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jooq.SQLDialect;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * A general SQL data source builder.
 *
 * @param <B> the type of the builder
 */
@SuppressWarnings("unchecked")
@AllArgsConstructor
abstract class ASqlDataSourceBuilder<B extends ASqlDataSourceBuilder<B>> {
    protected final @NotNull HikariConfig config;

    protected @Nullable String database;
    private @Nullable ExecutorService executor;

    /**
     * Creates a new SQL data source
     *
     * @return the SQL data source
     */
    public @NotNull SqlDataSource build() {
        config.setJdbcUrl(getJdbcUrl());
        ExecutorService executor = Objects.requireNonNull(this.executor, "executor has not been specified yet");
        return new SqlDataSource(new HikariDataSource(config), getSQLDialect(), executor);
    }

    /**
     * Gets the database name.
     *
     * @return the database
     */
    protected @NotNull String getDatabase() {
        return Objects.requireNonNull(database, "database name has not been specified yet");
    }

    /**
     * Gets the jdbc URL.
     *
     * @return the jdbc url
     */
    protected abstract @NotNull String getJdbcUrl();

    /**
     * Gets the SQL dialect.
     *
     * @return the SQL dialect
     */
    protected abstract @NotNull SQLDialect getSQLDialect();

    /**
     * Sets the database.
     *
     * @param database database
     * @return this object (for method chaining)
     */
    public @NotNull B database(final @Nullable String database) {
        this.database = database;
        return (B) this;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull B username(final @Nullable String username) {
        config.setUsername(username);
        return (B) this;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull B password(final @Nullable String password) {
        config.setPassword(password);
        return (B) this;
    }

    /**
     * Sets the maximum number of concurrent connections.
     * <br>
     * Default: <code>20</code>
     *
     * @param maximumPoolSize the maximum pool size
     * @return this object (for method chaining)
     */
    public @NotNull B maximumPoolSize(final @Range(from = 1, to = Integer.MAX_VALUE) int maximumPoolSize) {
        ValidationUtils.checkNatural(maximumPoolSize, "maximum pool size");
        config.setMaximumPoolSize(maximumPoolSize);
        return (B) this;
    }

    /**
     * Sets the minimum idle connections.
     * <br>
     * Default: <code>5</code>
     *
     * @param minimumIdle the minimum idle
     * @return this object (for method chaining)
     */
    public @NotNull B minimumIdle(final @Range(from = 0, to = Integer.MAX_VALUE) int minimumIdle) {
        ValidationUtils.checkPositive(minimumIdle, "minimum idle");
        config.setMinimumIdle(minimumIdle);
        return (B) this;
    }

    /**
     * Sets the connections timeout.
     * <br>
     * Default: <code>30000</code> (30 seconds)
     *
     * @param connectionTimeout the connection timeout
     * @return this object (for method chaining)
     */
    public @NotNull B connectionTimeout(final @Range(from = 1, to = Long.MAX_VALUE) long connectionTimeout) {
        ValidationUtils.checkNatural(connectionTimeout, "connection timeout");
        config.setConnectionTimeout(connectionTimeout);
        return (B) this;
    }

    /**
     * Sets the idle timeout for connections.
     * <br>
     * Default: <code>600000</code> (10 minutes)
     *
     * @param idleTimeout the idle timeout
     * @return this object (for method chaining)
     */
    public @NotNull B idleTimeout(final @Range(from = 0, to = Long.MAX_VALUE) long idleTimeout) {
        ValidationUtils.checkPositive(idleTimeout, "idle timeout");
        config.setIdleTimeout(idleTimeout);
        return (B) this;
    }

    /**
     * Sets the maximum lifetime of a connection.
     * <br>
     * Default: <code>1800000</code> (30 minutes)
     *
     * @param maxLifetime the max lifetime
     * @return this object (for method chaining)
     */
    public @NotNull B maxLifeTime(final @Range(from = 0, to = Long.MAX_VALUE) long maxLifetime) {
        ValidationUtils.checkPositive(maxLifetime, "maximum lifetime");
        config.setMaxLifetime(maxLifetime);
        return (B) this;
    }

    /**
     * Sets a property for the final datasource.
     *
     * @param propertyName the property name
     * @param value        the value
     * @return this object (for method chaining)
     */
    public @NotNull B addDataSourceProperty(final @NotNull String propertyName,
                                            final Object value) {
        config.addDataSourceProperty(propertyName, value);
        return (B) this;
    }

    /**
     * Sets the executor of the queries.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull B setExecutor(final @NotNull ExecutorService executor) {
        this.executor = executor;
        return (B) this;
    }

}
