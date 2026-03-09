package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.sql.DatabaseType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SqlDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                SqlDataSourceConfig.class,
                new SqlDataSourceFactory()
        );
    }

    @NotNull(message = "'database type' must be declared")
    @org.jetbrains.annotations.NotNull
    DatabaseType databaseType;

    @NotEmpty(message = "'database name' must not be empty")
    @org.jetbrains.annotations.NotNull
    String database;

    @Nullable
    String username;

    @Nullable
    String password;

    @Positive(message = "'maximum pool size' must be greater than 0")
    @Range(from = 1, to = Integer.MAX_VALUE)
    @Nullable
    Integer maximumPoolSize;

    @PositiveOrZero(message = "'minimum idle' must be at least 0")
    @Range(from = 0, to = Integer.MAX_VALUE)
    @Nullable
    Integer minimumIdle;

    @Positive(message = "'connection timeout' must be greater than 0")
    @Range(from = 1, to = Long.MAX_VALUE)
    @Nullable
    Long connectionTimeout;

    @PositiveOrZero(message = "'idle timeout' must be at least 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long idleTimeout;

    @PositiveOrZero(message = "'max life time' must be at least 0")
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

    @Min(value = 1, message = "'port' number must be at least 1")
    @Max(value = 65535, message = "'port' number must be at most 65535")
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

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConnectionMode {

        @NotNull
        @Builder.Default
        ConnectionModeType type = ConnectionModeType.MEMORY;

        @Nullable
        String directoryPath;

        @Nullable
        String host;

        @Min(value = 1, message = "'port' number must be at least 1")
        @Max(value = 65535, message = "'port' number must be at most 65535")
        @Range(from = 1, to = 65535)
        @Nullable
        Integer port;

    }

    public enum ConnectionModeType {
        MEMORY, DISK, SERVER
    }

}
