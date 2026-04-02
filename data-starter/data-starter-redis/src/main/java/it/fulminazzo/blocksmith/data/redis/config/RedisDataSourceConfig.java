package it.fulminazzo.blocksmith.data.redis.config;

import io.lettuce.core.RedisURI;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.validation.annotation.*;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class RedisDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                RedisDataSourceConfig.class,
                new RedisDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    @NotNull
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @Nullable
    @Builder.Default
    Integer port = RedisURI.DEFAULT_REDIS_PORT;

    @PositiveOrZero(exceptionMessage = "'database number' must be at least 0")
    @Nullable
    @Builder.Default
    Integer database = 0;

    @Nullable
    String clientName;

    @Nullable
    @Builder.Default
    Boolean ssl = false;

}
