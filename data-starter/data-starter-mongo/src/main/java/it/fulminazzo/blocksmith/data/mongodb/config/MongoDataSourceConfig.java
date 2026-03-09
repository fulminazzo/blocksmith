package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.ServerAddress;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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

    @NotNull(message = "'host' must be declared")
    String host;

    @Min(value = 1, message = "'port' number must be at least 1")
    @Max(value = 65535, message = "'port' number must be at most 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    @Builder.Default
    Integer port = ServerAddress.defaultPort();

    @Nullable
    String srvHost;

    @Positive(message = "'srv max hosts' must be greater than 0")
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

        @NotEmpty(message = "'username' must not be empty")
        String username;

        @NotEmpty(message = "'password' must not be empty")
        String password;

        @Nullable
        @Builder.Default
        String authSource = "admin";

        @Nullable
        String mechanism;

    }

}
