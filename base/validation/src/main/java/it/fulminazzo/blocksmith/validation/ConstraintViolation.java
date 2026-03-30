package it.fulminazzo.blocksmith.validation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the violation of a constraint.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
     * @param constraintInfo the constraint information to generate the messages from
     * @return the constraint violation
     */
    static @NotNull ConstraintViolation of(final Object value,
                                           final @NotNull ConstraintInfo constraintInfo) {
        String message = constraintInfo.getMessage();
        message = message == null
                ? null
                : message.replace("%value%", value == null ? "null" : value.toString());
        String defaultMessage = String.format(constraintInfo.getDefaultMessage(), constraintInfo.formatArguments(value));
        return new ConstraintViolation(value, message, defaultMessage);
    }


}
