package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import it.fulminazzo.blocksmith.data.Repository;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.concurrent.Executor;

/**
 * {@link DataSource} for general SQL databases.
 * <br>
 * Provides methods to create SQL repositories.
 */
public final class SqlDataSource implements DataSource, Closeable {
    @Delegate
    private final @NotNull HikariDataSource delegate;
    private final @NotNull DSLContext context;

    /**
     * Instantiates a new SQL data source.
     *
     * @param delegate the delegate that will handle the internal logic
     * @param dialect  the SQL dialect
     */
    SqlDataSource(final @NotNull HikariDataSource delegate,
                  final @NotNull SQLDialect dialect) {
        this.delegate = delegate;
        this.context = DSL.using(this, dialect);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>       the type of the data
     * @param <ID>      the type of the id
     * @param dataType  the data type
     * @param dataTable the data table
     * @param idColumn  the column of the id
     * @param executor  the executor (for asynchronous operations)
     * @return the repository
     */
    public <R extends Record, T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> dataType,
            final @NotNull Table<R> dataTable,
            final @NotNull TableField<R, ID> idColumn,
            final @NotNull Executor executor
    ) {
        return new SqlRepository<>(
                context,
                dataTable,
                idColumn,
                dataType,
                executor
        );
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull SqlDataSourceBuilder builder() {
        return new SqlDataSourceBuilder();
    }

}
