package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A builder for {@link SqlDataSource} for H2 databases.
 */
public final class H2DataSourceBuilder extends ASqlDataSourceBuilder {

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

}
