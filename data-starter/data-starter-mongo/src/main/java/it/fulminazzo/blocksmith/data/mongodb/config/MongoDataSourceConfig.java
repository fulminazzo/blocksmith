package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.ServerAddress;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSource;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.NotEmpty;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import it.fulminazzo.blocksmith.validation.annotation.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@With
public final class MongoDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                MongoDataSourceConfig.class,
                new MongoDataSourceFactory()
        );
    }

    @NonNull(exceptionMessage = "'host' must be declared")
    String host;

    @Port
    @Range(from = 1, to = 65535)
    @Nullable
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
    @With
    public static class MongoCredentialConfig {

        @NotEmpty(exceptionMessage = "'username' must not be empty")
        @NonNull
        String username;

        @NotEmpty(exceptionMessage = "'password' must not be empty")
        @NonNull
        String password;

        @Nullable
        @Builder.Default
        String authSource = "admin";

        @Nullable
        String mechanism;

    }

}
