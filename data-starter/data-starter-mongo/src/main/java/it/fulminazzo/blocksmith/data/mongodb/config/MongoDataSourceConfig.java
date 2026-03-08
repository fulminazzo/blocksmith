package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.ServerAddress;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@Value
@Builder
public class MongoDataSourceConfig implements DataSourceConfig {

    @NotNull(message = "host must be declared")
    String host;

    @Min(value = 1, message = "port number must be greater than or equal to 1")
    @Max(value = 65535, message = "port number must be lower than or equal to 65535")
    @Range(from = 1, to = 65535)
    @Nullable
    @Builder.Default
    Integer port = ServerAddress.defaultPort();

    @Nullable
    String srvHost;

    @Positive(message = "maxHosts must be greater than 0")
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

    @Value
    @Builder
    public static class MongoCredentialConfig {

        @NotEmpty(message = "username must not be empty")
        String username;

        @NotEmpty(message = "password must not be empty")
        String password;

        @Nullable
        @Builder.Default
        String authSource = "admin";

        @Nullable
        String mechanism;

    }

}
