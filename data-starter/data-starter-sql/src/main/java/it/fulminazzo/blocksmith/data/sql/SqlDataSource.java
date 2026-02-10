package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import it.fulminazzo.blocksmith.data.Repository;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * {@link DataSource} for general SQL databases.
 * <br>
 * Provides methods to create SQL repositories.
 * <br>
 * Example usage:
 * <ul>
 *     <li>general:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("username")
 *                 .password("password")
 *                 .database("database")
 *                 .build(); // will fail if a database type was not specified
 *          }
 *          </pre>
 *     Check {@link SqliteDataSourceBuilder} for more;</li>
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
     * @param <R>       the type of the record in the table
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
     * Creates a new custom repository.
     *
     * @param <R>                the type of the repository
     * @param <T>                the type of the data
     * @param <ID>               the type of the id
     * @param repositorySupplier the repository creation function
     * @param executor           the executor (for asynchronous operations)
     * @return the repository
     */
    public <R extends Repository<T, ID>, T, ID> @NotNull R newRepository(
            final @NotNull BiFunction<DSLContext, Executor, R> repositorySupplier,
            final @NotNull Executor executor
    ) {
        return repositorySupplier.apply(context, executor);
    }

    /**
     * Executes the given script.
     *
     * @param filePath the file to extract the script from
     * @return this object (for method chaining)
     * @throws IOException in case of any exception
     */
    public @NotNull SqlDataSource executeScriptFromFile(final @NotNull String filePath) throws IOException {
        return executeScriptFromFile(new File(filePath));
    }

    /**
     * Executes the given script.
     *
     * @param file the file to extract the script from
     * @return this object (for method chaining)
     * @throws IOException in case of any exception
     */
    public @NotNull SqlDataSource executeScriptFromFile(final @NotNull File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return executeScript(stream);
        }
    }

    /**
     * Executes the given script.
     *
     * @param resource the internal resource to extract the script from
     * @return this object (for method chaining)
     * @throws IOException in case of any exception
     */
    public @NotNull SqlDataSource executeScriptFromResource(final @NotNull String resource) throws IOException {
        try (InputStream stream = SqlDataSource.class.getResourceAsStream(resource)) {
            return executeScript(Objects.requireNonNull(stream, "Could not find resource: " + resource));
        }
    }

    /**
     * Executes the given script.
     *
     * @param stream the stream to extract the script from
     * @return this object (for method chaining)
     * @throws IOException in case of any exception
     */
    public @NotNull SqlDataSource executeScript(final @NotNull InputStream stream) throws IOException {
        byte[] data = stream.readAllBytes();
        String raw = new String(data, StandardCharsets.UTF_8);
        context.parser().parse(raw).executeBatch();
        return this;
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
