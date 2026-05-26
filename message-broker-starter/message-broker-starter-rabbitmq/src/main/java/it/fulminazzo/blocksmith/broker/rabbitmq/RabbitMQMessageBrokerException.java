package it.fulminazzo.blocksmith.broker.rabbitmq;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Unchecked exception used throughout this package to capture any RabbitMQ related exceptions.
 * <br>
 * For internal use only.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class RabbitMQMessageBrokerException extends RuntimeException {
    private static final long serialVersionUID = -9031030546876350320L;

    private RabbitMQMessageBrokerException(
            final @NotNull String message,
            final @NotNull Throwable cause,
            final @NotNull Object... args
    ) {
        super(String.format(message, args), cause);
    }

    static @NotNull RabbitMQMessageBrokerException createConnectionException(
            final @NotNull String host,
            final @NotNull Integer port,
            final @NotNull Throwable cause
    ) {
        return newException(
                "An exception occurred while instantiating a new connection to '%s:%s'",
                cause,
                host,
                port
        );
    }

    static @NotNull RabbitMQMessageBrokerException createChannelException(
            final @NotNull String exchangeName,
            @NotNull Throwable cause
    ) {
        return newException(
                "An exception occurred while creating a new channel for exchange '%s'",
                cause,
                exchangeName
        );
    }

    static @NotNull RabbitMQMessageBrokerException publishException(
            final @NotNull String payload,
            final @NotNull Throwable cause
    ) {
        return newException("An exception occurred while publishing payload '%s'", cause, payload);
    }

    static @NotNull RabbitMQMessageBrokerException queueDeclareException(
            final @NotNull String queueName,
            final @NotNull String exchangeName,
            final @NotNull String routingKey,
            @NotNull Throwable cause
    ) {
        return newException(
                "An exception occurred while declaring and binding queue '%s' to exchange '%s' with routing key '%s'",
                cause,
                queueName,
                exchangeName,
                routingKey
        );
    }

    static @NotNull RabbitMQMessageBrokerException registerConsumerException(
            final @NotNull String queueName,
            @NotNull Throwable cause
    ) {
        return newException("An exception occurred while registering consumer for queue '%s'", cause, queueName);
    }

    private static @NotNull RabbitMQMessageBrokerException newException(
            final @NotNull String message,
            @NotNull Throwable cause,
            @NotNull Object... args
    ) {
        cause = unwrapCause(cause);
        args = Arrays.copyOfRange(args, 0, args.length + 1);
        args[args.length - 1] = getCauseMessage(cause);
        RabbitMQMessageBrokerException exception = new RabbitMQMessageBrokerException(message + ": %s", cause, args);
        // remove static StackTrace elements
        StackTraceElement[] stackTrace = exception.getStackTrace();
        stackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
        exception.setStackTrace(stackTrace);
        return exception;
    }

    private static @NotNull String getCauseMessage(final @NotNull Throwable cause) {
        String message = cause.getMessage();
        return message != null ? message : cause.getClass().getSimpleName();
    }

    private static @NotNull Throwable unwrapCause(final @NotNull Throwable cause) {
        Throwable inner = cause.getCause();
        String message = cause.getMessage();
        return (message == null || message.isEmpty()) && inner != null ? unwrapCause(inner) : cause;
    }

}
