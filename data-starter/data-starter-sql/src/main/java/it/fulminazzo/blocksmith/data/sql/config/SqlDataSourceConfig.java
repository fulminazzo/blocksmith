package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.sql.DatabaseType;
import it.fulminazzo.blocksmith.validation.annotation.*;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class SqlDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                SqlDataSourceConfig.class,
                new SqlDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'database type' must be declared")
    @NotNull
    DatabaseType databaseType;

    @NotEmpty(exceptionMessage = "'database name' must not be empty")
    @NotNull
    String database;

    @Nullable
    String username;

    @Nullable
    String password;

    @Positive(exceptionMessage = "'maximum pool size' must be greater than 0")
    @Range(from = 1, to = Integer.MAX_VALUE)
    @Nullable
    Integer maximumPoolSize;

    @PositiveOrZero(exceptionMessage = "'minimum idle' must be at least 0")
    @Range(from = 0, to = Integer.MAX_VALUE)
    @Nullable
    Integer minimumIdle;

    @Positive(exceptionMessage = "'connection timeout' must be greater than 0")
    @Range(from = 1, to = Long.MAX_VALUE)
    @Nullable
    Long connectionTimeout;

    @PositiveOrZero(exceptionMessage = "'idle timeout' must be at least 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long idleTimeout;

    @PositiveOrZero(exceptionMessage = "'max life time' must be at least 0")
    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable
    Long maxLifeTime;

    @NotNull
    @Builder.Default
    Map<String, Object> properties = new HashMap<>();

    /*
     * RemoteDataSource
     */
    @Nullable
    String host;

    @Min(value = 1, exceptionMessage = "'port' number must be at least 1")
    @Max(value = 65535, exceptionMessage = "'port' number must be at most 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    Integer port;

    /*
     * H2DataSource
     */
    @Nullable
    String schemaName;

    @Builder.Default
    @NotNull
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

        @Min(value = 1, exceptionMessage = "'port' number must be at least 1")
        @Max(value = 65535, exceptionMessage = "'port' number must be at most 65535")
        @Range(from = 1, to = 65535)
        @Nullable
        Integer port;

    }

    public enum ConnectionModeType {
        MEMORY, DISK, SERVER
    }

}
