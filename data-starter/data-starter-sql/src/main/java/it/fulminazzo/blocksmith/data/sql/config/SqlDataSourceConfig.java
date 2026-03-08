package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.sql.IDatabaseType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class SqlDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                SqlDataSourceConfig.class,
                new SqlDataSourceFactory()
        );
    }

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

    @PositiveOrZero(message = "minimumIdle must be at least 0")
    @Range(from = 0, to = Integer.MAX_VALUE)
    @Nullable
    Integer minimumIdle;

    @Positive(message = "connectionTimeout must be greater than 0")
    @Range(from = 1, to = Long.MAX_VALUE)
    @Nullable
    Long connectionTimeout;

    @PositiveOrZero(message = "idleTimeout must be at least 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long idleTimeout;

    @PositiveOrZero(message = "maxLifeTime must be at least 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long maxLifeTime;

    @org.jetbrains.annotations.NotNull
    @Builder.Default
    Map<String, Object> properties = new HashMap<>();

    /*
     * RemoteDataSource
     */
    @Nullable
    String host;

    @Min(value = 1, message = "port number must be at least 1")
    @Max(value = 65535, message = "port number must be at most 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    Integer port;

    /*
     * H2DataSource
     */
    @Nullable
    String schemaName;

    @Builder.Default
    @org.jetbrains.annotations.NotNull
    Map<String, Object> parameters = new HashMap<>();

    /*
     * SqliteDataSource
     */

    @NotNull
    @Builder.Default
    ConnectionMode connectionMode = ConnectionMode.builder().build();

    @Value
    @Builder
    public static class ConnectionMode {

        @NotNull
        @Builder.Default
        ConnectionModeType type = ConnectionModeType.MEMORY;

        @Nullable
        String directoryPath;

        @Nullable
        String host;

        @Min(value = 1, message = "port number must be at least 1")
        @Max(value = 65535, message = "port number must be at most 65535")
        @Range(from = 1, to = 65535)
        @Nullable
        Integer port;

    }

    public enum ConnectionModeType {
        MEMORY, DISK, SERVER
    }

}
