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

    private static final double version = 4.0; // should be ignored

    private static final @NotNull ConfigVersion configVersion = ConfigVersion.of(3.0)
            .migrate(2.0, m -> m
                    .add("server.timeoutSeconds", 30)
                    .add("database.maxConnections", 100)
                    .add("features.enableBetaUi", true)
                    .add("features.enableMetrics", false)
            )
            .migrate(3.0, m -> {
                MigrationConfig defaultConfig = new MigrationConfig();
                return m
                        .add("environment", "production")
                        .rename("server.port", "server.ports.http")
                        .add("server.ports.https", defaultConfig.server.ports.https)
                        .remove("server.timeoutSeconds")
                        .add("server.ssl.enabled", defaultConfig.server.ssl.enabled)
                        .add("server.ssl.certPath", defaultConfig.server.ssl.certPath)
                        .add("server.ssl.keyPath", defaultConfig.server.ssl.keyPath)
                        .add("database.clusterMode", defaultConfig.database.clusterMode)
                        .rename("database.user", "database.credentials.user")
                        .rename("database.password", "database.credentials.password")
                        .remove("database.host")
                        .remove("database.port")
                        .add("database.pool.minSize", defaultConfig.database.pool.minSize)
                        .rename("database.maxConnections", "database.pool.maxSize")
                        .add("cache.provider", defaultConfig.cache.provider)
                        .add("cache.connectionString", defaultConfig.cache.connectionString)
                        .add("cache.ttlSeconds", defaultConfig.cache.ttlSeconds)
                        .remove("features.enableBetaUi")
                        .remove("features.enableMetrics")
                        .add("features.rollouts.newDashboard", defaultConfig.features.rollouts.newDashboard)
                        .add("features.rollouts.legacyApiDeprecation", defaultConfig.features.rollouts.legacyApiDeprecation);
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
