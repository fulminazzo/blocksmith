package it.fulminazzo.blocksmith.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public final class MigrationConfig {

    private static final @NotNull ConfigVersion configVersion = ConfigVersion.of(3.0)
            .migrate(2.0, m -> m
                    .add("server.timeout-seconds", 30)
                    .add("database.max-connections", 100)
                    .add("features.enable-beta-ui", true)
                    .add("features.enable-metrics", false)
            )
            .migrate(3.0, m -> {
                MigrationConfig defaultConfig = new MigrationConfig();
                return m
                        .add("environment", "production")
                        .rename("server.port", "server.ports.http")
                        .add("server.ports.https", defaultConfig.server.ports.https)
                        .remove("server.timeout-seconds")
                        .add("server.ssl.enabled", defaultConfig.server.ssl.enabled)
                        .add("server.ssl.cert-path", defaultConfig.server.ssl.certPath)
                        .add("server.ssl.key-path", defaultConfig.server.ssl.keyPath)
                        .add("database.cluster-mode", defaultConfig.database.clusterMode)
                        .rename("database.user", "database.credentials.user")
                        .rename("database.password", "database.credentials.password")
                        .remove("database.host")
                        .remove("database.port")
                        .add("database.pool.min-size", defaultConfig.database.pool.minSize)
                        .rename("database.max-connections", "database.pool.max-size")
                        .add("cache.provider", defaultConfig.cache.provider)
                        .add("cache.connection-string", defaultConfig.cache.connectionString)
                        .add("cache.ttl-seconds", defaultConfig.cache.ttlSeconds)
                        .remove("features.enable-beta-ui")
                        .remove("features.enable-metrics")
                        .add("features.rollouts.new-dashboard", defaultConfig.features.rollouts.newDashboard)
                        .add("features.rollouts.legacy-api-deprecation", defaultConfig.features.rollouts.legacyApiDeprecation);
            });

    @NotNull String environment = "production";

    @NotNull Server server = new Server();

    @NotNull Database database = new Database();

    @NotNull Cache cache = new Cache();

    @NotNull Features features = new Features();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static final class Server {

        @NotNull String host = "0.0.0.0";

        @NotNull Ports ports = new Ports();

        @NotNull Ssl ssl = new Ssl();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static final class Ports {

            int http = 8080;

            int https = 8443;

        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static final class Ssl {

            boolean enabled = true;

            @NotNull String certPath = "/etc/ssl/certs/app.crt";

            @NotNull String keyPath = "/etc/ssl/private/app.crt";

        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static final class Database {

        boolean clusterMode = true;

        @NotNull Credentials credentials = new Credentials();

        @NotNull Pool pool = new Pool();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static final class Credentials {

            @NotNull String user = "admin";

            @NotNull String password = "password123";

        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static final class Pool {

            int minSize = 10;

            int maxSize = 100;

        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static final class Cache {

        @NotNull String provider = "redis";

        @NotNull String connectionString = "redis://cache.internal.net:6379/0";

        int ttlSeconds = 3600;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static final class Features {

        @NotNull Rollouts rollouts = new Rollouts();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static final class Rollouts {

            double newDashboard = 0.5;

            boolean legacyApiDeprecation = true;

        }

    }

}
