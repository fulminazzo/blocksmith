package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import it.fulminazzo.blocksmith.data.util.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jooq.SQLDialect;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A builder for {@link SqlDataSource} for H2 databases.
 * <br>
 * Example usage:
 * <ul>
 *     <li>general:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("test")
 *                 .h2()
 *                 .lowercaseNames(true) // DATABASE_TO_LOWER=TRUE
 *                 .initScript("./resources/schema.sql") // INIT=RUNSCRIPT FROM ./resources/schema.sql
 *                 // defaults to database value
 *                 .schemaName("schema") // INIT=CREATE SCHEMA IF NOT EXISTS schema\;SET SCHEMA schema
 *                 .build();
 *          }</pre>
 *     </li>
 *     <li>in memory database:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("test")
 *                 .h2()
 *                 .memory() // mem:test
 *                 .preventMemoryLoss() // DB_CLOSE_DELAY=-1
 *                 .build();
 *          }</pre>
 *     </li>
 *     <li>file based database:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("test")
 *                 .h2()
 *                 .disk("./database") // creates the database ./database/test.mv.db
 *                 .allowSimultaneousFileConnections() // AUTO_SERVER=true
 *                 .preventConnectionOnNonExistingFile() // IFEXISTS=true
 *                 .build();
 *          }</pre>
 *     </li>
 *     <li>server based database:
 *          <pre>{@code
 *          SqlDataSource dataSource = SqlDataSource.builder()
 *                 .database("test")
 *                 .h2()
 *                 .server("localhost", 3306) // currently supports only tcp
 *                 .build();
 *          }</pre>
 *     </li>
 * </ul>
 */
public final class H2DataSourceBuilder extends ASqlDataSourceBuilder<H2DataSourceBuilder> {
    private static final String INITIAL_SETUP_KEY = "INIT";

    private final @NotNull Map<String, List<String>> parameters = new HashMap<>();

    private @Nullable String connectionMode;
    private @Nullable String schemaName;

    /**
     * Instantiates a new H2 data source builder.
     *
     * @param config   the config
     * @param database the database
     */
    H2DataSourceBuilder(final @NotNull HikariConfig config,
                        final @Nullable String database) {
        super(config, database);
    }

    @Override
    protected @NotNull String getJdbcUrl() {
        String schemaName = this.schemaName;
        if (schemaName == null) schemaName = getDatabase();
        setParameters(INITIAL_SETUP_KEY, "CREATE SCHEMA IF NOT EXISTS " + schemaName);
        setParameters(INITIAL_SETUP_KEY, "SET SCHEMA " + schemaName);
        return String.format("jdbc:h2:%s",
                Objects.requireNonNull(connectionMode, "The connection mode has not been specified yet. " +
                        "Please choose between memory, disk or server before building")
        ) + parameters.entrySet().stream()
                .map(e ->
                        String.format(";%s=%s", e.getKey(), String.join("\\;", e.getValue()))
                )
                .collect(Collectors.joining());
    }

    @Override
    protected @NotNull SQLDialect getSQLDialect() {
        return SQLDialect.H2;
    }

    /**
     * Sets the connection mode to memory (in RAM).
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder memory() {
        connectionMode = "mem:" + getDatabase();
        return this;
    }

    /**
     * Sets the connection mode to disk (using the file as storage).
     *
     * @param directoryPath the directory where the database file will be stored
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder disk(final @NotNull String directoryPath) {
        File directory = new File(directoryPath);
        connectionMode = new File(directory, getDatabase()).getAbsolutePath();
        return this;
    }

    /**
     * Opens the connection mode to the internet by using an internal server.
     *
     * @param host the host
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder server(final @NotNull String host,
                                               final @Range(from = 1, to = 65536) int port) {
        ValidationUtils.checkPort(port);
        connectionMode = String.format("tcp://%s:%s/%s", host, port, getDatabase());
        return this;
    }

    /**
     * Enables or disables lowercase names in the database.
     * <br>
     * Default: <code>true</code>
     *
     * @param value the value
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder lowercaseNames(final boolean value) {
        return setParameters("DATABASE_TO_LOWER", value);
    }

    /**
     * Sets the schema name to the given one.
     * <br>
     * Default: {@link #getDatabase()}
     *
     * @param name the name
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder schemaName(final @NotNull String name) {
        schemaName = name;
        return this;
    }

    /**
     * Allows to run the given <b>SQL</b> script on startup.
     *
     * @param filePath the path of the script
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder initScript(final @NotNull String filePath) {
        return setParameters(INITIAL_SETUP_KEY, String.format("RUNSCRIPT FROM '%s'", filePath));
    }

    /**
     * Prevents the database to lose data if every connection to it closes.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder preventMemoryLoss() {
        return setParameters("DB_CLOSE_DELAY", -1);
    }

    /**
     * Allows multiple users to connect to the same file based database.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder allowSimultaneousFileConnections() {
        return setParameters("AUTO_SERVER", true);
    }

    /**
     * Prevents the connection to the database if a file does not exist already.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder preventConnectionOnNonExistingFile() {
        return setParameters("IFEXISTS", true);
    }

    /**
     * Adds a new parameter to the final configuration of the database.
     *
     * @param name  the name of the parameter
     * @param value the value
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder setParameters(final @NotNull String name,
                                                      final @NotNull Object value) {
        return setParameters(name, value.toString().toUpperCase());
    }

    /**
     * Adds a new parameter to the final configuration of the database.
     *
     * @param name  the name of the parameter
     * @param value the value
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder setParameters(final @NotNull String name,
                                                      final @NotNull String value) {
        parameters.computeIfAbsent(name, v -> new ArrayList<>()).add(value);
        return this;
    }

}
