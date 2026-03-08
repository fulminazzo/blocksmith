package it.fulminazzo.blocksmith.data.sql.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactory;
import it.fulminazzo.blocksmith.data.sql.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

final class SqlDataSourceFactory implements DataSourceFactory {

    @Override
    public @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config) {
        SqlDataSourceConfig dsConfig = (SqlDataSourceConfig) config;
        SqlDataSourceBuilder builder = SqlDataSource.builder()
                .username(dsConfig.getUsername())
                .password(dsConfig.getPassword())
                .maximumPoolSize(dsConfig.getMaximumPoolSize())
                .minimumIdle(dsConfig.getMinimumIdle())
                .connectionTimeout(dsConfig.getConnectionTimeout())
                .idleTimeout(dsConfig.getIdleTimeout())
                .maxLifeTime(dsConfig.getMaxLifetime());
        @Nullable Map<String, Object> properties = dsConfig.getProperties();
        if (properties != null)
            properties.forEach((k, v) -> builder.addDataSourceProperty(k, v));

        IDatabaseType type = dsConfig.getDatabaseType();
        if (type == DatabaseType.H2) {
            H2DataSourceBuilder builder1 = builder.h2();
            SqlDataSourceConfig.ConnectionMode mode = Objects.requireNonNull(
                    dsConfig.getConnectionMode(),
                    "connection mode must be declared"
            );
            SqlDataSourceConfig.ConnectionModeType modeType = mode.getType();
            if (modeType == SqlDataSourceConfig.ConnectionModeType.MEMORY)
                builder1.memory();
            else if (modeType == SqlDataSourceConfig.ConnectionModeType.DISK)
                builder1.disk(Objects.requireNonNull(
                        mode.getDirectoryPath(),
                        "connection mode directoryPath must be declared"
                ));
            else if (modeType == SqlDataSourceConfig.ConnectionModeType.SERVER)
                builder1.server(
                        Objects.requireNonNull(
                                mode.getHost(),
                                "connection mode host must be declared"
                        ),
                        Objects.requireNonNull(
                                mode.getPort(),
                                "connection mode port must be declared"
                        )
                );
            builder1.schemaName(dsConfig.getSchemaName());
            @Nullable Map<String, Object> parameters = dsConfig.getParameters();
            if (parameters != null)
                parameters.forEach(builder1::setParameters);
            return builder1.build();
        }
        else if (type == DatabaseType.SQLITE) {
            SqliteDataSourceBuilder builder1 = builder.sqlite();
            SqlDataSourceConfig.ConnectionMode mode = Objects.requireNonNull(
                    dsConfig.getConnectionMode(),
                    "connection mode must be declared"
            );
            SqlDataSourceConfig.ConnectionModeType modeType = mode.getType();
            if (modeType == SqlDataSourceConfig.ConnectionModeType.MEMORY)
                builder1.memory();
            else if (modeType == SqlDataSourceConfig.ConnectionModeType.DISK)
                builder1.disk(Objects.requireNonNull(
                        mode.getDirectoryPath(),
                        "connection mode directoryPath must be declared"
                ));
            else throw new IllegalArgumentException(String.format("Unsupported connection mode '%s' for '%s'",
                        modeType, type
                ));
            return builder1.build();
        } else {
            RemoteDataSourceBuilder builder1 = builder.databaseType(type);
            if (type == DatabaseType.MYSQL || type == DatabaseType.MARIADB) builder1.mysql();
            else if (type == DatabaseType.POSTGRES) builder1.postgres();
            return builder1
                    .host(dsConfig.getHost())
                    .port(dsConfig.getPort())
                    .build();
        }
    }

}
