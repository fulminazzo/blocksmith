package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SocketOptions;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.Mappers;
import it.fulminazzo.blocksmith.data.util.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A builder for {@link RedisDataSource}.
 * <br>
 * Example usage:
 * <pre>{@code
 * RedisDataSource dataSource = RedisDataSource.builder()
 *         .encrypted(true) // defaults to false
 *         .username("username")
 *         .password("password")
 *         .host("127.0.0.1") // defaults to "0.0.0.0"
 *         .port(1339) // defaults to 6379
 *         .database(2) // defaults to 0
 *         .socketOptions(s -> s
 *                 .connectTimeout(Duration.ofSeconds(10))
 *                 .keepAlive(
 *                         SocketOptions.KeepAliveOptions.builder()
 *                                 .idle(Duration.ofSeconds(5))
 *                                 .interval(Duration.ofSeconds(5))
 *                                 .count(3)
 *                                 .enable()
 *                                 .build()
 *                 )
 *         )
 *         .clientOptions(c -> c
 *                 .autoReconnect(true)
 *                 .pingBeforeActivateConnection(true)
 *                 .requestQueueSize(4096)
 *         )
 *         .mapper(Mappers.SERIALIZABLE) // defaults to JSON
 *         .build();
 * }</pre>
 */
public final class RedisDataSourceBuilder {
    private final @NotNull ClientOptions.Builder clientOptions = ClientOptions.builder();
    private final @NotNull SocketOptions.Builder socketOptions = SocketOptions.builder();

    private boolean encrypted;

    private @Nullable String host;
    private int port;

    private int database;

    private @Nullable String username;
    private @Nullable String password;

    private @NotNull Mapper mapper = Mappers.JSON;

    /**
     * Instantiates a new Redis data source builder.
     */
    RedisDataSourceBuilder() {
        host("0.0.0.0").port(6379).database(0);
    }

    /**
     * Creates a new Redis data source.
     *
     * @return the redis data source
     */
    public @NotNull RedisDataSource build() {
        final RedisClient client = RedisClient.create(getRedisUrl());
        client.setOptions(clientOptions
                .socketOptions(socketOptions.build())
                .build()
        );
        return new RedisDataSource(client, mapper);
    }

    /**
     * Gets the Redis url.
     *
     * @return the redis url
     */
    @NotNull String getRedisUrl() {
        StringBuilder url = new StringBuilder("redis");
        if (encrypted) url.append("s");
        url.append("://");
        if (username != null || password != null) {
            String username = this.username == null ? "default" : this.username;
            url.append(URLEncoder.encode(username, StandardCharsets.UTF_8)).append(":");
            if (password != null) url.append(URLEncoder.encode(password, StandardCharsets.UTF_8));
            url.append("@");
        }
        return url
                .append(Objects.requireNonNull(host, "host has not been specified yet")).append(":")
                .append(port)
                .append("/")
                .append(database)
                .toString();
    }

    /**
     * Enables or disables encryption.
     * <br>
     * Default: <code>false</code>
     *
     * @param encrypted <code>true</code> to enable
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder encrypted(final boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    /**
     * Sets the host.
     * <br>
     * Default: <code>0.0.0.0</code>
     *
     * @param host the host
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder host(final @NotNull String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port.
     * <br>
     * Default: <code>6379</code>
     *
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder port(final @Range(from = 1, to = 65535) int port) {
        ValidationUtils.checkPort(port);
        this.port = port;
        return this;
    }

    /**
     * Sets the database.
     * <br>
     * Default: <code>0</code>
     *
     * @param database the database
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder database(final @Range(from = 0, to = 15) int database) {
        ValidationUtils.checkInRange(database, "database", 0, 15);
        this.database = database;
        return this;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder username(final @NotNull String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder password(final @NotNull String password) {
        this.password = password;
        return this;
    }

    /**
     * Applies the given function to the client options (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder clientOptions(final @NotNull Consumer<ClientOptions.Builder> function) {
        function.accept(clientOptions);
        return this;
    }

    /**
     * Applies the given function to the socket options (to update the options with new settings).
     *
     * @param function the function
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder socketOptions(final @NotNull Consumer<SocketOptions.Builder> function) {
        function.accept(socketOptions);
        return this;
    }

    /**
     * Sets the data mapper.
     * <br>
     * Because of Redis structure, it is necessary to serialize
     * and deserialize data before accessing to it.
     * The mapper is the component responsible for serialization.
     * <br>
     * Default: {@link Mappers#JSON}
     *
     * @param mapper the mapper
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder mapper(final @NotNull Mapper mapper) {
        this.mapper = mapper;
        return this;
    }

}
