package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A builder for {@link SqlDataSource} for host-port based databases.
 */
public final class RemoteDataSourceBuilder extends ASqlDataSourceBuilder {
    private final @NotNull IDatabaseType databaseType;
    private @Nullable String host;
    private @Nullable Integer port;

    /**
     * Instantiates a new Remote data source builder.
     *
     * @param config       the config
     * @param database     the database
     * @param databaseType the database type
     */
    RemoteDataSourceBuilder(final @NotNull HikariConfig config,
                            final @Nullable String database,
                            final @NotNull IDatabaseType databaseType) {
        super(config, database);
        this.databaseType = databaseType;
    }

    /**
     * Sets the host to connect to.
     *
     * @param host the host
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder host(final @NotNull String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port to connect to.
     *
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder port(final int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the internal configuration for MySQL and MariaDB.
     *
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder mySql() {
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
    public @NotNull RemoteDataSourceBuilder postgresql() {
        return addDataSourceProperty("tcpKeepAlive", true)
                .addDataSourceProperty("prepareThreshold", 5);
    }

    /**
     * Sets the internal configuration for Oracle.
     *
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder oracle() {
        return addDataSourceProperty("implicitCachingEnabled", true)
                .addDataSourceProperty("implicitStatementCacheSize", 100)
                .addDataSourceProperty("maxStatementsLimit", 250);
    }

}
