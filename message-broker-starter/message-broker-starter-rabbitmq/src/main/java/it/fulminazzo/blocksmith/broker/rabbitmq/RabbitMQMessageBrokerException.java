package it.fulminazzo.blocksmith.broker.rabbitmq;

import org.jetbrains.annotations.NotNull;

/**
 * Unchecked exception used throughout this package to capture any RabbitMQ related exceptions.
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
        return new RabbitMQMessageBrokerException(
                "An exception occurred while instantiating a new connection to '%s:%s': %s",
                cause,
                host,
                port,
                getCauseMessage(cause)
        );
    }

    static @NotNull RabbitMQMessageBrokerException createChannelException(
            final @NotNull String exchangeName,
            final @NotNull Throwable cause
    ) {
        return new RabbitMQMessageBrokerException(
                "An exception occurred while creating a new channel for exchange '%s': %s",
                cause,
                exchangeName,
                getCauseMessage(cause)
        );
    }

    static @NotNull RabbitMQMessageBrokerException publishException(
            final @NotNull String payload,
            final @NotNull Throwable cause
    ) {
        return new RabbitMQMessageBrokerException(
                "An exception occurred while publishing payload '%s': %s",
                cause,
                payload,
                getCauseMessage(cause)
        );
    }

    static @NotNull RabbitMQMessageBrokerException queueDeclareException(
            final @NotNull String queueName,
            final @NotNull String exchangeName,
            final @NotNull String routingKey,
            final @NotNull Throwable cause
    ) {
        return new RabbitMQMessageBrokerException(
                "An exception occurred while declaring and binding queue '%s' "
                        + "to exchange '%s' with routing key '%s': %s",
                cause,
                queueName,
                exchangeName,
                routingKey,
                getCauseMessage(cause)
        );
    }

    static @NotNull RabbitMQMessageBrokerException registerConsumerException(
            final @NotNull String queueName,
            final @NotNull Throwable cause
    ) {
        return new RabbitMQMessageBrokerException(
                "An exception occurred while registering consumer for queue '%s': %s",
                cause,
                queueName,
                getCauseMessage(cause)
        );
    }

    private static @NotNull String getCauseMessage(final @NotNull Throwable cause) {
        String message = cause.getMessage();
        return message != null ? message : cause.getClass().getSimpleName();
    }

}
