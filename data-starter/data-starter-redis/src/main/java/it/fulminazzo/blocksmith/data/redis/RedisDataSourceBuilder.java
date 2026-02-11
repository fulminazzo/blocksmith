package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.Mappers;
import it.fulminazzo.blocksmith.data.util.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

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
    public @NotNull RedisDataSourceBuilder host(final @Nullable String host) {
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
    public @NotNull RedisDataSourceBuilder username(final @Nullable String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull RedisDataSourceBuilder password(final @Nullable String password) {
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
