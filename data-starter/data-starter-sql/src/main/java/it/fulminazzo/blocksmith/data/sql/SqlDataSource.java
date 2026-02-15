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
import java.util.function.Function;

/**
 * SQL data source for handling connections and creating SQL-based repositories.
 * <br>
 * Supports multiple SQL databases through jOOQ: H2, SQLite, PostgreSQL, MySQL, Oracle, etc.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation (H2 in-memory):
 *         <pre>{@code
 *         SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("sa")
 *                 .password("")
 *                 .database("DATABASE")
 *                 .h2()
 *                 .memory()
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creation (SQLite on disk):
 *         <pre>{@code
 *         SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("database")
 *                 .sqlite()
 *                 .disk("./sqlite")
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creation (PostgreSQL):
 *         <pre>{@code
 *         SqlDataSource dataSource = SqlDataSource.builder()
 *                 .username("user")
 *                 .password("SuperSecurePassword")
 *                 .database("database")
 *                 .postgres()
 *                 .host("0.0.0.0")
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a standard repository:
 *         <pre>{@code
 *         SqlDataSource dataSource = ...;
 *
 *         Table<?> usersTable = DSL.table("users");
 *         TableField<?, Integer> idColumn = DSL.field("id", Integer.class);
 *
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new SqlRepositorySettings()
 *                         .withTable(usersTable)
 *                         .withIdColumn(idColumn)
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom repository:
 *         <pre>{@code
 *         SqlDataSource dataSource = ...;
 *
 *         Table<?> usersTable = DSL.table("users");
 *         TableField<?, Integer> idColumn = DSL.field("id", Integer.class);
 *
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 engine -> new CustomSqlRepository<>(engine),
 *                 new SqlRepositorySettings()
 *                         .withTable(usersTable)
 *                         .withIdColumn(idColumn)
 *         );
 *         }</pre>
 *         where CustomSqlRepository extends SqlRepository and adds custom behavior
 *         such as audit logging, transaction handling, or query optimization.
 *     </li>
 * </ul>
 */
public final class SqlDataSource implements RepositoryDataSource<SqlRepositorySettings> {
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

    @Override
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull SqlRepositorySettings settings
    ) {
        return newRepository(
                e -> new SqlRepository<>(e, entityMapper),
                settings
        );
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <ID>              the type of the id of the entities
     * @param <TR>              the type of the entities in the table
     * @param repositoryBuilder the repository creation function
     * @param settings          the settings to build the repository with
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    public <R extends SqlRepository<T, ID, Table<TR>>, T, ID, TR extends Record> @NotNull R newRepository(
            final @NotNull Function<SqlQueryEngine<T, ID, Table<TR>>, R> repositoryBuilder,
            final @NotNull SqlRepositorySettings settings
    ) {
        SqlQueryEngine<T, ID, Table<TR>> engine = new SqlQueryEngine<>(
                context,
                (Table<TR>) settings.getTable(),
                (Field<ID>) settings.getIdColumn(),
                executor
        );
        return repositoryBuilder.apply(engine);
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
