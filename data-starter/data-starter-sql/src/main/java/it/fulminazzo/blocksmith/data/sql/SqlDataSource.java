package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * SQL datasource for handling connection and create repositories.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("username")
 *                 .password("password")
 *                 .database("database")
 *                 .build(); // will fail if a database type was not specified
 *          }
 *          </pre>
 *     Check {@link SqliteDataSourceBuilder} for more;
 *     </li>
 *     <li>H2 connection creation:
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
 *     <li>SQLite connection creation:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("database")
 *                 .sqlite()
 *                 .disk("./sqlite")
 *                 .build();
 *          }
 *          </pre>
 *     Check {@link SqliteDataSourceBuilder} for more;
 *     </li>
 *     <li>other remote SQL database connection creation:
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
 *     Check {@link RemoteDataSourceBuilder} for more.
 *     </li>
 *     <li>creating a new repository:
 *         <pre>{@code
 *         SqlDataSource dataSource = ...;
 *         Class<?> dataType = ...;
 *         // The table in the database containing the entities.
 *         // Supports jOOQ generation plugin (for sub-implementations).
 *         Table<?> entitiesTable = ...;
 *         // The column that identifies the entities in the table.
 *         TableField<?, ?> tableIdColumn = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 dataType,
 *                 entitiesTable,
 *                 tableIdColumn
 *         );
 *         }</pre>
 *         or, for more control:
 *         <pre>{@code
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(dataType),
 *                 entitiesTable,
 *                 tableIdColumn
 *         );
 *         }</pre>
 *     </li>
 * </ul>
 */
public final class SqlDataSource implements RepositoryDataSource {
    private final @NotNull HikariDataSource dataSource;
    private final @NotNull DSLContext context;
    private final @NotNull ExecutorService executor;

    /**
     * Instantiates a new SQL data source.
     *
     * @param dataSource the data source
     * @param dialect    the dialect
     * @param executor   the executor
     */
    SqlDataSource(final @NotNull HikariDataSource dataSource,
                  final @NotNull SQLDialect dialect,
                  final @NotNull ExecutorService executor) {
        this.dataSource = dataSource;
        this.executor = executor;
        this.context = DSL.using(dataSource, dialect);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>        the type of the entities
     * @param <ID>       the type of the id of the entities
     * @param <R>        the type of the entities in the table
     * @param entityType the entity Java class
     * @param table      the table
     * @param idColumn   the column that represents the ID of the entities in the table
     * @return the repository
     */
    public <T, ID, R extends Record> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull Table<R> table,
            final @NotNull TableField<R, ID> idColumn
    ) {
        return newRepository(EntityMapper.create(entityType), table, idColumn);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>          the type of the entities
     * @param <ID>         the type of the id of the entities
     * @param <R>          the type of the entities in the table
     * @param entityMapper the entities mapper
     * @param table        the table
     * @param idColumn     the column that represents the ID of the entities in the table
     * @return the repository
     */
    public <T, ID, R extends Record> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull Table<R> table,
            final @NotNull TableField<R, ID> idColumn
    ) {
        SqlQueryEngine<T, ID, Table<R>> engine = new SqlQueryEngine<>(
                context,
                table,
                idColumn,
                executor
        );
        return new SqlRepository<>(engine, entityMapper);
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

    @Override
    public void close() {
        executor.shutdown();
        dataSource.close();
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
