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
 *          }
 *          </pre>
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
 *          }
 *          </pre>
 *     Check {@link H2DataSourceBuilder} for more;</li>
 *     <li>sqlite:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("database")
 *                 .sqlite()
 *                 .disk("./sqlite")
 *                 .build();
 *          }
 *          </pre>
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
 *          }
 *          </pre>
 *     Check {@link RemoteDataSourceBuilder} for more.</li>
 * </ul>
 */
public final class SqlDataSourceBuilder extends ASqlDataSourceBuilder<SqlDataSourceBuilder> {
    private static final int maximumPoolSize = 20;
    private static final int minimumIdle = 5;
    private static final long connectionTimeout = 30 * 1000;
    private static final long idleTimeout = 10 * 60 * 1000;
    private static final long maxLifeTime = 30 * 60 * 1000;

    /**
     * Instantiates a new SQL data source builder.
     */
    SqlDataSourceBuilder() {
        super(new HikariConfig(), null);
        maximumPoolSize(maximumPoolSize)
                .minimumIdle(minimumIdle)
                .connectionTimeout(connectionTimeout)
                .idleTimeout(idleTimeout)
                .maxLifeTime(maxLifeTime);
    }

    @Override
    protected @NotNull String getJdbcUrl() {
        throw new IllegalStateException("A database type has not been set yet! " +
                "Please use setDatabaseType or h2 before calling this method");
    }

    @Override
    protected @NotNull SQLDialect getSQLDialect() {
        throw new IllegalStateException("A database type has not been set yet! " +
                "Please use setDatabaseType or h2 before calling this method");
    }

    /**
     * Sets the database type.
     *
     * @param databaseType database type
     * @return this object (for method chaining)
     */
    public @NotNull RemoteDataSourceBuilder databaseType(final @NotNull IDatabaseType databaseType) {
        return new RemoteDataSourceBuilder(config, database, databaseType);
    }

    /**
     * Sets the database type to H2.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder h2() {
        return new H2DataSourceBuilder(config, database).lowercaseNames(true);
    }

    /**
     * Sets the database type to SQLite.
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqliteDataSourceBuilder sqlite() {
        return new SqliteDataSourceBuilder(config, database);
    }

}
