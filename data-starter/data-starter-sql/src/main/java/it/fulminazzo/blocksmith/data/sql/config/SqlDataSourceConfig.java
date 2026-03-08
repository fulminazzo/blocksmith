package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.sql.IDatabaseType;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

@Value
public class SqlDataSourceConfig implements DataSourceConfig {

    @NotNull(message = "databaseType must be declared")
    @org.jetbrains.annotations.NotNull
    IDatabaseType databaseType;

    @NotEmpty(message = "database name must not be empty")
    @org.jetbrains.annotations.NotNull
    String database;

    @Nullable
    String username;

    @Nullable
    String password;

    @Positive(message = "maximumPoolSize must be greater than 0")
    @Range(from = 1, to = Integer.MAX_VALUE)
    @Nullable
    Integer maximumPoolSize;

    @PositiveOrZero(message = "minimumIdle must be greater than or equal to 0")
    @Range(from = 0, to = Integer.MAX_VALUE)
    @Nullable
    Integer minimumIdle;

    @Positive(message = "connectionTimeout must be greater than 0")
    @Range(from = 1, to = Long.MAX_VALUE)
    @Nullable
    Long connectionTimeout;

    @PositiveOrZero(message = "idleTimeout must be greater than or equal to 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long idleTimeout;

    @PositiveOrZero(message = "maxLifetime must be greater than or equal to 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long maxLifetime;

    @Nullable
    Map<String, Object> properties;

    /*
     * RemoteDataSource
     */
    @Nullable
    String host;

    @Min(value = 1, message = "port number must be greater than or equal to 1")
    @Max(value = 1, message = "port number must be lower than or equal to 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    Integer port;

    /*
     * H2DataSource
     */
    @Nullable
    String schemaName;

    @Nullable
    Map<String, Object> parameters;

    /*
     * SqliteDataSource
     */

    @Nullable
    ConnectionMode connectionMode;

    public static abstract class ConnectionMode {

    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class H2ConnectionMode extends ConnectionMode {

        @NotNull
        H2ConnectionModeType type = H2ConnectionModeType.MEMORY;

        @Nullable
        String directoryPath;

        @Nullable
        String host;

        @Min(value = 1, message = "port number must be greater than or equal to 1")
        @Max(value = 1, message = "port number must be lower than or equal to 65535")
        @Range(from = 1, to = 65535)
        @Nullable
        Integer port;

    }

    public enum H2ConnectionModeType {
        MEMORY, DISK, SERVER
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class SqliteConnectionMode extends ConnectionMode {

        @NotNull
        SqliteConnectionModeType type = SqliteConnectionModeType.MEMORY;

        @Nullable
        String directoryPath;

    }

    public enum SqliteConnectionModeType {
        MEMORY, DISK
    }

}
