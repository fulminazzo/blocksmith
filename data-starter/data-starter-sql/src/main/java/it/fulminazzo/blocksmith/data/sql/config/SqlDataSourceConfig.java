package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.sql.DatabaseType;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.validation.annotation.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SqlDataSourceConfig} for {@link SqlDataSource}.
 *
 * @see DataSourceConfig
 * @see SqlDataSource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public final class SqlDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                SqlDataSourceConfig.class,
                new SqlDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'database type' must be declared")
    DatabaseType databaseType;

    @NonNull(exceptionMessage = "'database name' must not be empty")
    @NotEmpty(exceptionMessage = "'database name' must not be empty")
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

    Map<String, Object> properties = new HashMap<>();

    /*
     * RemoteDataSource
     */
    @Nullable
    String host;

    @Port
    @Nullable
    Integer port;

    /*
     * H2DataSource
     */
    @Nullable
    String schemaName;

    Map<String, Object> parameters = new HashMap<>();

    /*
     * H2DataSource and SqliteDataSource
     */

    ConnectionMode connectionMode = new ConnectionMode();

    /**
     * Defines the connection mode type.
     */
    public enum ConnectionModeType {
        MEMORY, DISK, SERVER
    }

    /**
     * Defines the connection mode.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static class ConnectionMode {

        ConnectionModeType type = ConnectionModeType.MEMORY;

        /**
         * The path of the directory where the database should be saved.
         * The database will be saved in the form {@code <directory_path>/<database_name>.mv.db}
         */
        @Nullable
        String directoryPath;

    }

}
