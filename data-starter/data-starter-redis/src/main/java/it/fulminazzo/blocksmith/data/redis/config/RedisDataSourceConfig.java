package it.fulminazzo.blocksmith.data.redis.config;

import io.lettuce.core.RedisURI;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@Value
@Builder
public class RedisDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                RedisDataSourceConfig.class,
                new RedisDataSourceFactory()
        );
    }

    @NotNull(message = "'host' must be declared")
    String host;

    @Min(value = 1, message = "'port' number must be at least 1")
    @Max(value = 65535, message = "'port' number must be at most 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    @Builder.Default
    Integer port = RedisURI.DEFAULT_REDIS_PORT;

    @PositiveOrZero(message = "'database number' must be at least 0")
    @Nullable
    @Builder.Default
    Integer database = 0;

    @Nullable
    String clientName;

    @Nullable
    @Builder.Default
    Boolean ssl = false;


}
