package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.ServerAddress;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSource;
import it.fulminazzo.blocksmith.validation.annotation.*;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * {@link MongoDataSourceConfig} for {@link MongoDataSource}.
 *
 * @see DataSourceConfig
 * @see MongoDataSource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class MongoDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                MongoDataSourceConfig.class,
                new MongoDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    @NotNull
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @Nullable
    @Builder.Default
    Integer port = ServerAddress.defaultPort();

    @Nullable
    String srvHost;

    @Positive(exceptionMessage = "'srv max hosts' must be greater than 0")
    @Range(from = 1, to = Integer.MAX_VALUE)
    @Nullable
    Integer srvMaxHosts;

    @Nullable
    String srvServiceName;

    @Nullable
    String replicaSetName;

    @Nullable
    String applicationName;

    @Nullable
    MongoCredentialConfig credentials;

    /**
     * MongoDB credentials configuration.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MongoCredentialConfig {

        @NotEmpty(exceptionMessage = "'username' must not be empty")
        @NonNull
        @NotNull
        String username;

        @NotEmpty(exceptionMessage = "'password' must not be empty")
        @NonNull
        @NotNull
        String password;

        @Nullable
        @Builder.Default
        String authSource = "admin";

        @Nullable
        String mechanism;

    }

}
