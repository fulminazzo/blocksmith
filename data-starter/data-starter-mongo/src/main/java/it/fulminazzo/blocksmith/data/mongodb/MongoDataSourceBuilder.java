package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.LoggerSettings;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.*;
import com.mongodb.reactivestreams.client.MongoClients;
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder;
import it.fulminazzo.blocksmith.data.util.ValidationUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * A builder for {@link MongoDataSource}.
 * <br>
 * Example usage:
 * <pre>{@code
 * MongoDataSource datasource = MongoDataSource.builder()
 *         .srvHost("cluster.host.com")
 *         .srvMaxHosts(10)
 *         .srvServiceName("mongodb")
 *         // if no host has been specified, will default to "0.0.0.0:27017"
 *         .host("mongodb1.host.com", 27017)
 *         .host("mongodb2.host.com", 27017)
 *         .host("mongodb3.host.com", 27017)
 *         .replicaSetName("rs0")
 *         .credential(MongoCredential.createScramSha256Credential(
 *                 "username",
 *                 "admin",
 *                 "password".toCharArray()
 *         ))
 *         .applicationName("blocksmith/1.0.0")
 *         .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
 *         // default value
 *         .codecRegistry(CodecRegistries.fromRegistries(
 *                 MongoClientSettings.getDefaultCodecRegistry(),
 *                 CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
 *         ))
 *         .loggerSettings(l -> l.maxDocumentLength(5000))
 *         .clusterSettings(c -> c.mode(ClusterConnectionMode.LOAD_BALANCED))
 *         .socketSettings(s -> s
 *                 .connectTimeout(15, TimeUnit.SECONDS)
 *                 .readTimeout(30, TimeUnit.SECONDS)
 *         )
 *         .connectionPoolSettings(cp -> cp
 *                 .minSize(10)
 *                 .maxSize(200)
 *                 .maxConnectionIdleTime(15, TimeUnit.MINUTES)
 *         )
 *         .serverSettings(s -> s.heartbeatFrequency(10, TimeUnit.SECONDS))
 *         .sslSettings(ssl -> ssl
 *                 .enabled(true)
 *                 .invalidHostNameAllowed(false)
 *         )
 *         .build();
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class MongoDataSourceBuilder implements RepositoryDataSourceBuilder<MongoDataSource> {
    private static final @NotNull ServerAddress defaultAddress = new ServerAddress("0.0.0.0");
    private static final @NotNull CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
    );

    private final @NotNull MongoClientSettings.Builder clientSettings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry);

    private final @NotNull List<ServerAddress> hosts = new ArrayList<>();

    @Override
    public @NonNull MongoDataSource build() {
        if (hosts.isEmpty()) hosts.add(defaultAddress);
        clientSettings.applyToClusterSettings(c -> c.hosts(hosts));
        return new MongoDataSource(MongoClients.create(clientSettings.build()));
    }

    /**
     * Sets the srv host for the connection.
     *
     * @param srvHost the srv host
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder srvHost(final @NotNull String srvHost) {
        return clusterSettings(c -> c.srvHost(srvHost));
    }

    /**
     * Sets the srv maximum number of hosts for the connection.
     *
     * @param maxHosts the max hosts
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder srvMaxHosts(final @Range(from = 1, to = Integer.MAX_VALUE) int maxHosts) {
        ValidationUtils.checkNatural(maxHosts, "srv maximum number of hosts");
        return clusterSettings(c -> c.srvMaxHosts(maxHosts));
    }

    /**
     * Sets the srv service name for the connection.
     *
     * @param srvServiceName the srv service name
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder srvServiceName(final @NotNull String srvServiceName) {
        return clusterSettings(c -> c.srvServiceName(srvServiceName));
    }

    /**
     * Adds a new host to the connection.
     *
     * @param address the host address
     * @param port    the port
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder host(final @NotNull String address,
                                                final @Range(from = 1, to = 65535) int port) {
        ValidationUtils.checkPort(port);
        hosts.add(new ServerAddress(address, port));
        return this;
    }

    /**
     * Sets the replica set name for the connection.
     *
     * @param replicaSetName the replica set name
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder replicaSetName(final @NotNull String replicaSetName) {
        return clusterSettings(c -> c.requiredReplicaSetName(replicaSetName));
    }


    /**
     * Sets the credentials for the client.
     *
     * @param credential the credentials
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder credential(final @NotNull MongoCredential credential) {
        clientSettings.credential(credential);
        return this;
    }

    /**
     * Sets the credentials for the client.
     * <br>
     * Default: the default POJO codec registry
     *
     * @param codecRegistry the codec registry
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder codecRegistry(final @NotNull CodecRegistry codecRegistry) {
        clientSettings.codecRegistry(codecRegistry);
        return this;
    }

    /**
     * Sets the credentials for the client.
     *
     * @param applicationName the application name
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder applicationName(final @NotNull String applicationName) {
        clientSettings.applicationName(applicationName);
        return this;
    }

    /**
     * Sets the credentials for the client.
     *
     * @param uuidRepresentation the uuid representation
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder uuidRepresentation(final @NotNull UuidRepresentation uuidRepresentation) {
        clientSettings.uuidRepresentation(uuidRepresentation);
        return this;
    }


    /**
     * Applies the given function to the logger settings (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder loggerSettings(final @NotNull Consumer<LoggerSettings.Builder> function) {
        clientSettings.applyToLoggerSettings(function::accept);
        return this;
    }

    /**
     * Applies the given function to the cluster settings (to update the options with new settings).
     * <br>
     * <b>WARNING: some methods might override the specified configuration.</b>
     * <br>
     * Check {@link #host(String, int)} for more.
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder clusterSettings(final @NotNull Consumer<ClusterSettings.Builder> function) {
        clientSettings.applyToClusterSettings(function::accept);
        return this;
    }

    /**
     * Applies the given function to the socket settings (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder socketSettings(final @NotNull Consumer<SocketSettings.Builder> function) {
        clientSettings.applyToSocketSettings(function::accept);
        return this;
    }

    /**
     * Applies the given function to the connection pool settings (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder connectionPoolSettings(final @NotNull Consumer<ConnectionPoolSettings.Builder> function) {
        clientSettings.applyToConnectionPoolSettings(function::accept);
        return this;
    }

    /**
     * Applies the given function to the server settings (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder serverSettings(final @NotNull Consumer<ServerSettings.Builder> function) {
        clientSettings.applyToServerSettings(function::accept);
        return this;
    }

    /**
     * Applies the given function to the SSL settings (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull MongoDataSourceBuilder sslSettings(final @NotNull Consumer<SslSettings.Builder> function) {
        clientSettings.applyToSslSettings(function::accept);
        return this;
    }

}
