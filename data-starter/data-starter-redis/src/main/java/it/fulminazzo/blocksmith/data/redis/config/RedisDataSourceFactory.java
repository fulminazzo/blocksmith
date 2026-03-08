package it.fulminazzo.blocksmith.data.redis.config;

import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactory;
import it.fulminazzo.blocksmith.data.redis.RedisDataSource;
import org.jetbrains.annotations.NotNull;

final class RedisDataSourceFactory implements DataSourceFactory {

    @Override
    public @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config) {
        RedisDataSourceConfig dsConfig = (RedisDataSourceConfig) config;
        return RedisDataSource.builder()
                .uri(uri -> {
                    uri.withHost(dsConfig.getHost())
                            .withClientName(dsConfig.getClientName())
                            .withSsl(Boolean.TRUE.equals(dsConfig.getSsl()));

                    Integer port = dsConfig.getPort();
                    if (port != null) uri.withPort(port);

                    Integer database = dsConfig.getDatabase();
                    if (database != null) uri.withDatabase(database);
                })
                .build();
    }

}
