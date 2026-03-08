package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.sql.IDatabaseType;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;

@Value
public class SqlDataSourceConfig implements DataSourceConfig {

    @NotNull IDatabaseType databaseType;

    @NotNull String database;

    @Nullable String username;

    @Nullable String password;

    @Range(from = 1, to = Integer.MAX_VALUE)
    @Nullable Integer maximumPoolSize;

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Nullable Integer minimumIdle;

    @Range(from = 1, to = Long.MAX_VALUE)
    @Nullable Long connectionTimeout;

    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable Long idleTimeout;

    @Range(from = 0, to = Long.MAX_VALUE)
    @Nullable Long maxLifetime;

    @Nullable Map<String, Object> properties;

    /*
     * RemoteDataSource
     */
    @Nullable String host;

    @Range(from = 1, to = 65535)
    @Nullable Integer port;

    /*
     * H2DataSource
     */
    @Nullable String schemaName;

    @Nullable Map<String, Object> parameters;

    /*
     * SqliteDataSource
     */

    @Nullable ConnectionMode connectionMode;

    public static abstract class ConnectionMode {

    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class H2ConnectionMode extends ConnectionMode {

        @NotNull H2ConnectionModeType type;

        @Nullable String directoryPath;

        @Nullable String host;

        @Range(from = 1, to = 65535)
        @Nullable Integer port;

    }

    public enum H2ConnectionModeType {
        MEMORY, DISK, SERVER
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class SqliteConnectionMode extends ConnectionMode {

        @NotNull SqliteConnectionModeType type;

        @Nullable String directoryPath;

    }

    public enum SqliteConnectionModeType {
        MEMORY, DISK
    }

}
