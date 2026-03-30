package it.fulminazzo.blocksmith.validation;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the violation of a constraint.
 */
@Value
public class ConstraintViolation {
    Object value;

    /**
     * The reason why there are two messages is the following:
     * <ul>
     *     <li>the first message is user-defined, but it falls back to a message code
     *     later translated by an appropriate translator;</li>
     *     <li>the second message is static and used during throwing of exceptions
     *     in Java code.</li>
     * </ul>
     */
    @Nullable String message;
    @NotNull String defaultMessage;

    /**
     * Instantiates a new Constraint violation.
     *
     * @param value          the value
     * @param message        the message
     * @param defaultMessage the default message
     */
    public ConstraintViolation(final Object value,
                               final @Nullable String message,
                               final @NotNull String defaultMessage) {
        this.value = value;
        this.message = message == null
                ? null
                : message.replace("%value%", value == null ? "null" : value.toString());
        this.defaultMessage = String.format(defaultMessage, message);
    }


}
