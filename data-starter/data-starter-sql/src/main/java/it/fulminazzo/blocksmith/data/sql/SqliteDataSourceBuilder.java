package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SQLDialect;

import java.io.File;
import java.util.Objects;

/**
 * A builder for {@link SqlDataSource} for SQLite databases.
 */
public final class SqliteDataSourceBuilder extends ASqlDataSourceBuilder<SqliteDataSourceBuilder> {
    private @Nullable String connectionMode;

    /**
     * Instantiates a new SQLite data source builder.
     *
     * @param config   the config
     * @param database the database
     */
    SqliteDataSourceBuilder(final @NotNull HikariConfig config,
                            final @Nullable String database) {
        super(config, database);
    }

    @Override
    protected @NotNull String getJdbcUrl() {
        return String.format("jdbc:sqlite:%s",
                Objects.requireNonNull(connectionMode, "The connection mode has not been specified yet. " +
                        "Please choose between memory, disk or server before building")
        );
    }

    @Override
    protected @NotNull SQLDialect getSQLDialect() {
        return SQLDialect.SQLITE;
    }

    /**
     * Sets the connection mode to memory (in RAM).
     *
     * @return this object (for method chaining)
     */
    public @NotNull SqliteDataSourceBuilder memory() {
        connectionMode = ":memory:";
        return this;
    }

    /**
     * Sets the connection mode to disk (using the file as storage).
     *
     * @param directoryPath the directory where the database file will be stored
     * @return this object (for method chaining)
     */
    public @NotNull SqliteDataSourceBuilder disk(final @NotNull String directoryPath) {
        File directory = new File(directoryPath);
        connectionMode = new File(directory, getDatabase() + ".db").getAbsolutePath();
        return this;
    }

}
