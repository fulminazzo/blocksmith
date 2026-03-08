package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import it.fulminazzo.blocksmith.util.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jooq.SQLDialect;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * A builder for {@link SqlDataSource} for host-port based databases.
 * <br>
 * Example usage:
 * <pre>{@code
 * SqlDataSource dataSource = SqlDataSource.builder()
 *        .username("username")
 *        .password("password")
 *        .database("test")
 *        .databaseType(databaseType)
 *        .host("0.0.0.0") // defaults to "127.0.0.1"
 *        .port(1337) // defaults to the port of the database type
 *        .mysql() // optimizations for MySQL/MariaDB
 *        .postgres() // optimizations for PostgreSQL
 *        .build();
 * }</pre>
 */
public final class RemoteDataSourceBuilder extends ASqlDataSourceBuilder<RemoteDataSourceBuilder> {
    private final @NotNull IDatabaseType databaseType;
    private @Nullable String host;
    private @Nullable Integer port;

    /**
     * Instantiates a new Remote data source builder.
     *
     * @param config       the config
     * @param database     the database
     * @param executor     the executor
     * @param databaseType the database type
     */
    RemoteDataSourceBuilder(final @NotNull HikariConfig config,
                            final @Nullable String database,
                            final @Nullable ExecutorService executor,
                            final @NotNull IDatabaseType databaseType) {
        super(config, database, executor);
        this.databaseType = databaseType;
        host("127.0.0.1").port(databaseType.getPort());
    }

    @Override
    protected @NotNull String getJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s",
                databaseType.getJdbcName(),
                Objects.requireNonNull(host, "host has not been specified yet"),
                Objects.requireNonNull(port, "port has not been specified yet"),
                getDatabase()
        );
    }

    @Override
    protected @NotNull SQLDialect getSQLDialect() {
        try {
            return SQLDialect.valueOf(databaseType.getJdbcName().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SQLDialect.DEFAULT;
        }
    }

    /**
     * Sets the host to connect to.
     * <br>
     * Default: <code>127.0.0.1</code>
     *
     * @param host the host
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder host(final @Nullable String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port to connect to.
     * <br>
     * Default: {@link IDatabaseType#getPort()}
     *
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder port(final @Nullable @Range(from = 1, to = 65535) Integer port) {
        if (port != null) {
            ValidationUtils.checkPort(port);
            this.port = port;
        }
        return this;
    }

    /**
     * Sets the internal configuration for MySQL and MariaDB.
     *
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder mysql() {
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
    public @NotNull RemoteDataSourceBuilder postgres() {
        return addDataSourceProperty("tcpKeepAlive", true)
                .addDataSourceProperty("prepareThreshold", 5);
    }

}
