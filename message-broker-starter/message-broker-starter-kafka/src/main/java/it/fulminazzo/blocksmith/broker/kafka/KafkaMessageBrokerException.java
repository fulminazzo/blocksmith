package it.fulminazzo.blocksmith.broker.kafka;

import org.jetbrains.annotations.NotNull;

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
            final @NotNull Object... args
    ) {
        super(String.format(message, args));
    }

    static @NotNull KafkaMessageBrokerException getAssignmentException(
            final @NotNull Collection<String> topics,
            final long waitInterval
    ) {
        return newException(
                "Unable to get assignment for topics '%s' after %s ms",
                String.join(", ", topics),
                waitInterval
        );
    }

    private static @NotNull KafkaMessageBrokerException newException(
            final @NotNull String message,
            final @NotNull Object... args
    ) {
        KafkaMessageBrokerException exception = new KafkaMessageBrokerException(message, args);
        // remove static StackTrace elements
        StackTraceElement[] stackTrace = exception.getStackTrace();
        stackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
        exception.setStackTrace(stackTrace);
        return exception;
    }

}
