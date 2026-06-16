package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.AbstractMessageBrokerBuilder;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import it.fulminazzo.blocksmith.validation.Validator;
import it.fulminazzo.blocksmith.validation.annotation.Port;
import it.fulminazzo.blocksmith.validation.annotation.PositiveOrZero;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A builder for {@link TcpMessageBroker}.
 * <br>
 * Example usage:
 * <pre>{@code
 * TcpMessageBroker messageBroker = TcpMessageBroker.builder()
 *         .port(12345) // defaults to 30926
 *         .retryInterval(5_000L) // defaults to 1_000L
 *         .mapper(MapperFormat.SERIALIZABLE.newMapper()) // defaults to JSON
 *         .logger(logger)
 *         .executor(executor)
 *         .build();
 * }</pre>
 *
 * @see TcpMessageBroker
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class TcpMessageBrokerBuilder
        extends AbstractMessageBrokerBuilder<TcpMessageBroker, TcpMessageBrokerBuilder> {
    private @Nullable Logger logger;
    private @Nullable ExecutorService executor;

    private int port = 30926;
    private long retryInterval = 1_000L;

    /**
     * Sets the port the client and server will use to communicate.
     * <br>
     * Default: {@code 30926}
     *
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessageBrokerBuilder port(final @Port @NotNull Integer port) {
        Validator.validateMethod(port);
        this.port = port;
        return this;
    }

    /**
     * Sets the interval between retries when the connection is lost.
     * <br>
     * Default: {@code 1 second}
     *
     * @param retryInterval the interval
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessageBrokerBuilder retryInterval(final @PositiveOrZero @NotNull Long retryInterval) {
        Validator.validateMethod(retryInterval);
        this.retryInterval = retryInterval;
        return this;
    }

    /**
     * Sets the logger used to display messages.
     *
     * @param logger the logger
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessageBrokerBuilder logger(final @NotNull Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Sets the executor of the queries.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull TcpMessageBrokerBuilder executor(final @NotNull ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull TcpMessageBroker build() {
        final Logger actualLogger;
        if (logger != null) actualLogger = logger;
        else actualLogger = LoggerFactory.getLogger(TcpMessageChannel.class);

        final ExecutorService actualExecutor;
        if (executor != null) actualExecutor = executor;
        else actualExecutor = Executors.newCachedThreadPool(
                ThreadUtils.ownedThreadFactory(TcpMessageQueryEngine.class)
        );

        return new TcpMessageBroker(
                port,
                retryInterval,
                getMapper(),
                actualLogger,
                actualExecutor
        );
    }

}
