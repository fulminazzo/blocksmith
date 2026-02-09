package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A builder for {@link SqlDataSource} for H2 databases.
 */
public final class H2DataSourceBuilder extends ASqlDataSourceBuilder<H2DataSourceBuilder> {
    private final @NotNull Map<String, String> parameters = new HashMap<>();

    private @Nullable String connectionMode;

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
        return String.format("jdbc:h2:%s:%s",
                Objects.requireNonNull(connectionMode, "The connection mode has not been specified yet. " +
                        "Please choose between memory, disk or server before building"),
                getDatabase()
        ) + parameters.entrySet().stream()
                .map(e -> String.format(";%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining());
    }

    /**
     * Sets the connection mode to memory (in RAM).
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder memory() {
        connectionMode = "mem";
        return this;
    }

    /**
     * Sets the connection mode to disk (using the file as storage).
     *
     * @param filePath the file path
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder disk(@NotNull String filePath) {
        if (filePath.endsWith("/")) filePath = filePath.substring(0, filePath.length() - 1);
        connectionMode = filePath;
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
                                               final int port) {
        connectionMode = String.format("tcp://%s:%s", host, port);
        return this;
    }

    /**
     * Prevents the database to lose data if every connection to it closes.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder preventMemoryLoss() {
        return setParameters("DB_CLOSE_DELAY", "-1");
    }

    /**
     * Allows multiple users to connect to the same file based database.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder allowSimultaneousFileConnections() {
        return setParameters("AUTO_SERVER", "TRUE");
    }

    /**
     * Prevents the connection to the database if a file does not exist already.
     *
     * @return this object (for method chaining)
     */
    public @NotNull H2DataSourceBuilder preventConnectionOnNonExistingFile() {
        return setParameters("IFEXISTS", "TRUE");
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
        parameters.put(name.toUpperCase(), value.toUpperCase());
        return this;
    }

}
