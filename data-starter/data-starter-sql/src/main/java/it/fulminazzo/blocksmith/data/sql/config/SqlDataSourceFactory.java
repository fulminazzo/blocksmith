package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactory;
import it.fulminazzo.blocksmith.data.sql.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

final class SqlDataSourceFactory implements DataSourceFactory {

    @Override
    public @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config) {
        SqlDataSourceConfig dsConfig = (SqlDataSourceConfig) config;
        SqlDataSourceBuilder builder = SqlDataSource.builder()
                .executor(Executors.newCachedThreadPool())
                .database(dsConfig.getDatabase())
                .username(dsConfig.getUsername())
                .password(dsConfig.getPassword())
                .maximumPoolSize(dsConfig.getMaximumPoolSize())
                .minimumIdle(dsConfig.getMinimumIdle())
                .connectionTimeout(dsConfig.getConnectionTimeout())
                .idleTimeout(dsConfig.getIdleTimeout())
                .maxLifeTime(dsConfig.getMaxLifeTime());
        @Nullable Map<String, Object> properties = dsConfig.getProperties();
        if (properties != null)
            properties.forEach((k, v) -> builder.addDataSourceProperty(k, v));

        IDatabaseType type = dsConfig.getDatabaseType();
        if (type == DatabaseType.H2) return buildH2(builder, dsConfig);
        else if (type == DatabaseType.SQLITE) return buildSQLite(builder, dsConfig);
        else return buildRemote(builder, dsConfig, type);
    }

    @NotNull SqlDataSource buildH2(final @NotNull SqlDataSourceBuilder dataSourceBuilder,
                                   final @NotNull SqlDataSourceConfig config) {
        H2DataSourceBuilder builder = dataSourceBuilder.h2();
        SqlDataSourceConfig.ConnectionMode mode = Objects.requireNonNull(
                config.getConnectionMode(),
                String.format("connection mode must be declared for %s database", DatabaseType.H2)
        );
        SqlDataSourceConfig.ConnectionModeType modeType = mode.getType();
        if (modeType == SqlDataSourceConfig.ConnectionModeType.MEMORY)
            builder.memory();
        else if (modeType == SqlDataSourceConfig.ConnectionModeType.DISK)
            builder.disk(Objects.requireNonNull(
                    mode.getDirectoryPath(),
                    "connection mode directoryPath must be declared"
            ));
        else if (modeType == SqlDataSourceConfig.ConnectionModeType.SERVER)
            builder.server(
                    Objects.requireNonNull(
                            mode.getHost(),
                            "connection mode host must be declared"
                    ),
                    Objects.requireNonNull(
                            mode.getPort(),
                            "connection mode port must be declared"
                    )
            );
        builder.schemaName(config.getSchemaName());
        @Nullable Map<String, Object> parameters = config.getParameters();
        if (parameters != null)
            parameters.forEach(builder::setParameters);
        return builder.build();
    }

    @NotNull SqlDataSource buildSQLite(final @NotNull SqlDataSourceBuilder dataSourceBuilder,
                                       final @NotNull SqlDataSourceConfig config) {
        final DatabaseType type = DatabaseType.SQLITE;

        SqliteDataSourceBuilder builder = dataSourceBuilder.sqlite();
        SqlDataSourceConfig.ConnectionMode mode = Objects.requireNonNull(
                config.getConnectionMode(),
                String.format("connection mode must be declared for %s database", type)
        );
        SqlDataSourceConfig.ConnectionModeType modeType = mode.getType();
        if (modeType == SqlDataSourceConfig.ConnectionModeType.MEMORY)
            builder.memory();
        else if (modeType == SqlDataSourceConfig.ConnectionModeType.DISK)
            builder.disk(Objects.requireNonNull(
                    mode.getDirectoryPath(),
                    "connection mode directoryPath must be declared"
            ));
        else throw new IllegalArgumentException(String.format("Unsupported connection mode '%s' for '%s'",
                    modeType, type
            ));
        return builder.build();
    }

    @NotNull SqlDataSource buildRemote(final @NotNull SqlDataSourceBuilder dataSourceBuilder,
                                       final @NotNull SqlDataSourceConfig config,
                                       final @NotNull IDatabaseType type) {
        RemoteDataSourceBuilder builder = dataSourceBuilder.databaseType(type);
        if (type == DatabaseType.MYSQL || type == DatabaseType.MARIADB) builder.mysql();
        else if (type == DatabaseType.POSTGRESQL) builder.postgres();
        return builder
                .host(config.getHost())
                .port(config.getPort())
                .build();
    }

}
