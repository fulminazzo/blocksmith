package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.ServerAddress;
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

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
