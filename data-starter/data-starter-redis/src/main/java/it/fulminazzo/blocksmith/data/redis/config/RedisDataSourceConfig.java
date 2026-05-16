package it.fulminazzo.blocksmith.data.redis.config;

import io.lettuce.core.RedisURI;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.redis.RedisDataSource;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * {@link DataSourceConfig} for {@link RedisDataSource}.
 *
 * @see DataSourceConfig
 * @see RedisDataSource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@With
public final class RedisDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                RedisDataSourceConfig.class,
                new RedisDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @Nullable
    Integer port = RedisURI.DEFAULT_REDIS_PORT;

    @PositiveOrZero(exceptionMessage = "'database number' must be at least 0")
    @Nullable
    Integer database = 0;

    @Nullable
    String clientName;

    @Nullable
    Boolean ssl = false;

}
