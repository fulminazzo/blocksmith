package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;

/**
 * A builder for {@link SqlDataSource}.
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

}
