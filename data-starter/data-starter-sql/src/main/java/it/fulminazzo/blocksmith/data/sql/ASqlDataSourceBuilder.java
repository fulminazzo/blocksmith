package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jooq.SQLDialect;

import java.util.Objects;

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

    /**
     * Creates a new SQL data source
     *
     * @return the SQL data source
     */
    public @NotNull SqlDataSource build() {
        config.setJdbcUrl(getJdbcUrl());
        return new SqlDataSource(new HikariDataSource(config), getSQLDialect());
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
     * Sets username.
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull B username(final @Nullable String username) {
        config.setUsername(username);
        return (B) this;
    }

    /**
     * Sets password.
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
     *
     * @param maximumPoolSize the maximum pool size
     * @return this object (for method chaining)
     */
    public @NotNull B maximumPoolSize(final @Range(from = 1, to = Integer.MAX_VALUE) int maximumPoolSize) {
        config.setMaximumPoolSize(maximumPoolSize);
        return (B) this;
    }

    /**
     * Sets the minimum idle connections.
     *
     * @param minimumIdle the minimum idle
     * @return this object (for method chaining)
     */
    public @NotNull B minimumIdle(final @Range(from = 0, to = Integer.MAX_VALUE) int minimumIdle) {
        config.setMinimumIdle(minimumIdle);
        return (B) this;
    }

    /**
     * Sets the connections timeout.
     *
     * @param connectionTimeout the connection timeout
     * @return this object (for method chaining)
     */
    public @NotNull B connectionTimeout(final @Range(from = 1, to = Long.MAX_VALUE) long connectionTimeout) {
        config.setConnectionTimeout(connectionTimeout);
        return (B) this;
    }

    /**
     * Sets the idle timeout for connections.
     *
     * @param idleTimeout the idle timeout
     * @return this object (for method chaining)
     */
    public @NotNull B idleTimeout(final @Range(from = 0, to = Long.MAX_VALUE) long idleTimeout) {
        config.setIdleTimeout(idleTimeout);
        return (B) this;
    }

    /**
     * Sets the maximum lifetime of a connection.
     *
     * @param maxLifetime the max lifetime
     * @return this object (for method chaining)
     */
    public @NotNull B maxLifeTime(final @Range(from = 0, to = Long.MAX_VALUE) long maxLifetime) {
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

}
