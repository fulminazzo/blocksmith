package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;

/**
 * A builder for {@link SqlDataSource}.
 * <br>
 * Example usage:
 * <ul>
 *     <li>general:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("username")
 *                 .password("password")
 *                 .database("database")
 *                 .maximumPoolSize(20) // default = 20
 *                 .minimumIdle(5) // default = 5
 *                 .connectionTimeout(30000) // default = 30 seconds
 *                 .idleTimeout(600000) // default = 10 minutes
 *                 .maxLifeTime(1800000) // default = 30 minutes
 *                 .addDataSourceProperty("cachePrepStmts", true)
 *                 .build(); // will fail if a database type was not specified
 *          }</pre>
 *     </li>
 *     <li>h2:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("sa")
 *                 .password("")
 *                 .database("DATABASE")
 *                 .h2()
 *                 .memory()
 *                 .build();
 *          }</pre>
 *     Check {@link H2DataSourceBuilder} for more;</li>
 *     <li>sqlite:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("database")
 *                 .sqlite()
 *                 .disk("./sqlite")
 *                 .build();
 *          }</pre>
 *     Check {@link SqliteDataSourceBuilder} for more;</li>
 *     <li>remote:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("user")
 *                 .password("SuperSecurePassword")
 *                 .database("database")
 *                 .databaseType(DatabaseType.POSTGRES)
 *                 .postgres()
 *                 .build();
 *          }</pre>
 *     Check {@link RemoteDataSourceBuilder} for more.</li>
 * </ul>
 *
 * @see SqlDataSource
 * @see SqlDataSourceBuilder
 * @see H2DataSourceBuilder
 * @see SqliteDataSourceBuilder
 * @see RemoteDataSourceBuilder
 */
public final class SqlDataSourceBuilder extends ASqlDataSourceBuilder<SqlDataSourceBuilder> {
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int MINIMUM_IDLE = 5;
    private static final long CONNECTION_TIMEOUT = 30 * 1000L;
    private static final long IDLE_TIMEOUT = 10 * 60 * 1000L;
    private static final long MAX_LIFE_TIME = 30 * 60 * 1000L;

    /**
     * Instantiates a new SQL data source builder.
     */
    SqlDataSourceBuilder() {
        super(new HikariConfig(), null, null);
        maximumPoolSize(MAXIMUM_POOL_SIZE)
                .minimumIdle(MINIMUM_IDLE)
                .connectionTimeout(CONNECTION_TIMEOUT)
                .idleTimeout(IDLE_TIMEOUT)
                .maxLifeTime(MAX_LIFE_TIME);
    }

    /**
     * Sets the database type.
     *
     * @param databaseType database type
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder databaseType(final @NotNull IDatabaseType databaseType) {
        return new RemoteDataSourceBuilder(config, database, executor, databaseType);
    }

    /**
     * Sets the database type to H2.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder h2() {
        return new H2DataSourceBuilder(config, database, executor).lowercaseNames(true);
    }

    /**
     * Sets the database type to SQLite.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqliteDataSourceBuilder sqlite() {
        return new SqliteDataSourceBuilder(config, database, executor);
    }

    @Override
    protected @NotNull String getJdbcUrl() {
        throw new IllegalStateException("A database type has not been set yet! "
                + "Please use setDatabaseType or h2 before calling this method");
    }

    @Override
    protected @NotNull SQLDialect getSQLDialect() {
        throw new IllegalStateException("A database type has not been set yet! "
                + "Please use setDatabaseType or h2 before calling this method");
    }

}
