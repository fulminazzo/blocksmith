package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A builder for {@link SqlRepository}.
 */
public final class SqlDataSourceBuilder {
    private static final int maximumPoolSize = 20;
    private static final int minimumIdle = 5;
    private static final long connectionTimeout = 30 * 1000;
    private static final long idleTimeout = 10 * 60 * 1000;
    private static final long maxLifeTime = 30 * 60 * 1000;

    private final @NotNull HikariConfig config;

    private @Nullable String databaseType;
    private @Nullable String host;
    private @Nullable Integer port;
    private @Nullable String database;

    /**
     * Instantiates a new SQL data source builder.
     */
    SqlDataSourceBuilder() {
        this.config = new HikariConfig();
        maximumPoolSize(maximumPoolSize)
                .minimumIdle(minimumIdle)
                .connectionTimeout(connectionTimeout)
                .idleTimeout(idleTimeout)
                .maxLifeTime(maxLifeTime);
    }

    /**
     * Sets the databaseType.
     *
     * @param databaseType databaseType
     * @return (this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setDatabaseType(final @Nullable String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    /**
     * Sets the host.
     *
     * @param host host
     * @return (this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setHost(final @Nullable String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port.
     *
     * @param port port
     * @return (this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setPort(final @Nullable Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the database.
     *
     * @param database database
     * @return (this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setDatabase(final @Nullable String database) {
        this.database = database;
        return this;
    }

    /**
     * Sets username.
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setUsername(final @Nullable String username) {
        config.setUsername(username);
        return this;
    }

    /**
     * Sets password.
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder setPassword(final @Nullable String password) {
        config.setPassword(password);
        return this;
    }

    /**
     * Sets the maximum number of concurrent connections.
     *
     * @param maximumPoolSize the maximum pool size
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder maximumPoolSize(final int maximumPoolSize) {
        config.setMaximumPoolSize(maximumPoolSize);
        return this;
    }

    /**
     * Sets the minimum idle connections.
     *
     * @param minimumIdle the minimum idle
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder minimumIdle(final int minimumIdle) {
        config.setMinimumIdle(minimumIdle);
        return this;
    }

    /**
     * Sets the connections timeout.
     *
     * @param connectionTimeout the connection timeout
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder connectionTimeout(final long connectionTimeout) {
        config.setConnectionTimeout(connectionTimeout);
        return this;
    }

    /**
     * Sets the idle timeout for connections.
     *
     * @param idleTimeout the idle timeout
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder idleTimeout(final long idleTimeout) {
        config.setIdleTimeout(idleTimeout);
        return this;
    }

    /**
     * Sets the maximum lifetime of a connection.
     *
     * @param maxLifetime the max lifetime
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder maxLifeTime(final long maxLifetime) {
        config.setMaxLifetime(maxLifetime);
        return this;
    }

    /**
     * Sets the internal configuration for MySQL and MariaDB.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder mySql() {
        return addDataSourceProperty("cachePrepStmts", true)
                .addDataSourceProperty("prepStmtCacheSize", 250)
                .addDataSourceProperty("prepStmtCacheSqlLimit", 2048)
                .addDataSourceProperty("useServerPrepStmts", true)
                .addDataSourceProperty("useLocalSessionState", true)
                .addDataSourceProperty("rewriteBatchedStatements", true)
                .addDataSourceProperty("cacheResultSetMetadata", true)
                .addDataSourceProperty("cacheServerConfiguration", true)
                .addDataSourceProperty("elideSetAutoCommits", true)
                .addDataSourceProperty("maintainTimeStats", false);
    }

    /**
     * Sets the internal configuration for Postgres.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder postgres() {
        return addDataSourceProperty("tcpKeepAlive", true)
                .addDataSourceProperty("prepareThreshold", 5);
    }

    /**
     * Sets the internal configuration for Oracle.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder oracle() {
        return addDataSourceProperty("implicitCachingEnabled", true)
                .addDataSourceProperty("implicitStatementCacheSize", 100)
                .addDataSourceProperty("maxStatementsLimit", 250);
    }

    /**
     * Sets a property for the final datasource.
     *
     * @param propertyName the property name
     * @param value        the value
     * @return this object (for method chaining)
     */
    public @NotNull SqlDataSourceBuilder addDataSourceProperty(final @NotNull String propertyName,
                                                               final Object value) {
        config.addDataSourceProperty(propertyName, value);
        return this;
    }

}
