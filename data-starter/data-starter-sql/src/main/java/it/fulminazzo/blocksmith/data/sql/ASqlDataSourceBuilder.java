package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
abstract class ASqlDataSourceBuilder {

    protected final @NotNull HikariConfig config;

    protected @Nullable String database;

    /**
     * Sets the database.
     *
     * @param database database
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder setDatabase(final @Nullable String database) {
        this.database = database;
        return this;
    }

    /**
     * Sets username.
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder setUsername(final @Nullable String username) {
        config.setUsername(username);
        return this;
    }

    /**
     * Sets password.
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder setPassword(final @Nullable String password) {
        config.setPassword(password);
        return this;
    }

    /**
     * Sets the maximum number of concurrent connections.
     *
     * @param maximumPoolSize the maximum pool size
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder maximumPoolSize(final int maximumPoolSize) {
        config.setMaximumPoolSize(maximumPoolSize);
        return this;
    }

    /**
     * Sets the minimum idle connections.
     *
     * @param minimumIdle the minimum idle
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder minimumIdle(final int minimumIdle) {
        config.setMinimumIdle(minimumIdle);
        return this;
    }

    /**
     * Sets the connections timeout.
     *
     * @param connectionTimeout the connection timeout
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder connectionTimeout(final long connectionTimeout) {
        config.setConnectionTimeout(connectionTimeout);
        return this;
    }

    /**
     * Sets the idle timeout for connections.
     *
     * @param idleTimeout the idle timeout
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder idleTimeout(final long idleTimeout) {
        config.setIdleTimeout(idleTimeout);
        return this;
    }

    /**
     * Sets the maximum lifetime of a connection.
     *
     * @param maxLifetime the max lifetime
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder maxLifeTime(final long maxLifetime) {
        config.setMaxLifetime(maxLifetime);
        return this;
    }

    /**
     * Sets a property for the final datasource.
     *
     * @param propertyName the property name
     * @param value        the value
     * @return this object (for method chaining)
     */
    public @NotNull ASqlDataSourceBuilder addDataSourceProperty(final @NotNull String propertyName,
                                                                final Object value) {
        config.addDataSourceProperty(propertyName, value);
        return this;
    }

}
