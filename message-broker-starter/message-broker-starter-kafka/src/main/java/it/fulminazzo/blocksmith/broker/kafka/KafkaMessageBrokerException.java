package it.fulminazzo.blocksmith.broker.kafka;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unchecked exception used throughout this package to capture any Kafka related exceptions.
 * <br>
 * For internal use only.
 */
public final class KafkaMessageBrokerException extends RuntimeException {
    private static final long serialVersionUID = -7620010484920439395L;

    private KafkaMessageBrokerException(
            final @NotNull String message,
            final @Nullable Throwable cause,
            final @NotNull Object... args
    ) {
        super(String.format(message, args), cause);
    }

    static @NotNull KafkaMessageBrokerException publishException(
            final @NotNull String topic,
            final @NotNull String payload,
            final @NotNull Throwable cause
    ) {
        return newException(
                "An exception occurred while publishing payload '%s' to topic '%s'",
                cause,
                payload,
                topic
        );
    }

    static @NotNull KafkaMessageBrokerException getAssignmentException(
            final @NotNull Collection<String> topics,
            final long waitInterval
    ) {
        return newException(
                "Unable to get assignment for topics '%s' after %s ms",
                null,
                String.join(", ", topics),
                waitInterval
        );
    }

    private static @NotNull KafkaMessageBrokerException newException(
            @NotNull String message,
            @Nullable Throwable cause,
            @NotNull Object... args
    ) {
        if (cause != null) {
            message = message + ": %s";
            cause = unwrapCause(cause);
            args = Arrays.copyOfRange(args, 0, args.length + 1);
            args[args.length - 1] = getCauseMessage(cause);
        }
        KafkaMessageBrokerException exception = new KafkaMessageBrokerException(message, cause, args);
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
