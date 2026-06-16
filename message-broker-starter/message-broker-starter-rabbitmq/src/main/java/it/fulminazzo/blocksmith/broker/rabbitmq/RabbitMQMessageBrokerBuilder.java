package it.fulminazzo.blocksmith.broker.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import it.fulminazzo.blocksmith.broker.AbstractMessageBrokerBuilder;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.annotation.IPv4;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * A builder for {@link RabbitMQMessageBroker}.
 * <br>
 * Example usage:
 * <pre>{@code
 * RabbitMQMessageBroker messageBroker = RabbitMQMessageBroker.builder()
 *         .host("0.0.0.0") // defaults to "127.0.0.1"
 *         .port(5671) // defaults to 5672
 *         .username("root") // defaults to "guest"
 *         .password("super-secure-password-should-use-an-env-variable") // defaults to "guest"
 *         .configure(f -> f
 *                 .setConnectionTimeout(30_000)
 *                 .setHandshakeTimeout(10_000)
 *         )
 *         .mapper(MapperFormat.SERIALIZABLE.newMapper()) // defaults to JSON
 *         .build();
 * }</pre>
 *
 * @see RabbitMQMessageBroker
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class RabbitMQMessageBrokerBuilder
        extends AbstractMessageBrokerBuilder<RabbitMQMessageBroker, RabbitMQMessageBrokerBuilder> {
    private final @NotNull ConnectionFactory connectionFactory;

    private @Nullable ExecutorService executor;

    /**
     * Instantiates a new RabbitMQ message broker builder.
     */
    RabbitMQMessageBrokerBuilder() {
        this.connectionFactory = new ConnectionFactory();
    }

    /**
     * Sets the host to connect to.
     * <br>
     * Default: {@link ConnectionFactory#DEFAULT_HOST}
     *
     * @param host the host
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder host(final @NotNull String host) {
        Validator.validateMethod(host);
        this.connectionFactory.setHost(host);
        return this;
    }

    /**
     * Sets the port to connect to.
     * <br>
     * Default: {@link ConnectionFactory#DEFAULT_AMQP_PORT} (or {@link ConnectionFactory#DEFAULT_AMQP_OVER_SSL_PORT}
     * if SSL has been enabled)
     *
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder port(final @Port @NotNull Integer port) {
        Validator.validateMethod(port);
        this.connectionFactory.setPort(port);
        return this;
    }

    /**
     * Sets the username to connect with.
     * <br>
     * Default: {@link ConnectionFactory#DEFAULT_USER}
     *
     * @param username the username
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder username(final @NotNull String username) {
        this.connectionFactory.setUsername(username);
        return this;
    }

    /**
     * Sets the password to connect with.
     * <br>
     * Default: {@link ConnectionFactory#DEFAULT_PASS}
     *
     * @param password the password
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder password(final @NotNull String password) {
        this.connectionFactory.setPassword(password);
        return this;
    }

    /**
     * Allows directly configuring the internal {@link #connectionFactory}.
     *
     * @param settings the settings to apply
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder configure(final @NotNull Consumer<ConnectionFactory> settings) {
        settings.accept(connectionFactory);
        return this;
    }

    /**
     * Sets the executor of the queries.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull RabbitMQMessageBrokerBuilder executor(final @NotNull ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull RabbitMQMessageBroker build() {
        final boolean generatedExecutor;
        final ExecutorService actualExecutor;
        if (executor != null) {
            generatedExecutor = false;
            actualExecutor = executor;
        } else {
            generatedExecutor = true;
            actualExecutor = Executors.newCachedThreadPool(
                    ThreadUtils.ownedThreadFactory(RabbitMQMessageQueryEngine.class)
            );
        }

        final Connection connection;
        try {
            connection = connectionFactory.newConnection(actualExecutor);
        } catch (IOException | TimeoutException e) {
            if (generatedExecutor) actualExecutor.shutdown();
            throw RabbitMQMessageBrokerException.createConnectionException(
                    connectionFactory.getHost(),
                    connectionFactory.getPort(),
                    e
            );
        }

        return new RabbitMQMessageBroker(actualExecutor, connection, getMapper());
    }

}
