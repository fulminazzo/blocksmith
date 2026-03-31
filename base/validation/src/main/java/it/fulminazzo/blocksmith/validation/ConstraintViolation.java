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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConstraintViolation {
    Object value;

    /**
     * The reason why there are two messages is the following:
     * <ul>
     *     <li>the first message is user-defined, but it falls back to a message code
     *     later translated by an appropriate translator;</li>
     *     <li>the second message is used in throwing of exceptions in Java code.</li>
     * </ul>
     */
    @Nullable String message;
    @NotNull String exceptionMessage;

    /**
     * Instantiates a new Constraint violation to signal an invalid type violation.
     *
     * @param value             the value
     * @param expectedTypeNames the expected types names
     * @return the constraint violation
     */
    static @NotNull ConstraintViolation invalidType(final Object value,
                                                    final @NotNull String expectedTypeNames) {
        return new ConstraintViolation(value,
                "error.validation.invalid-type",
                String.format("Expected %s but got '%s'", expectedTypeNames, value));
    }

    /**
     * Instantiates a new Constraint violation.
     *
     * @param value          the value
     * @param constraintInfo the constraint information to generate the messages from
     * @return the constraint violation
     */
    static @NotNull ConstraintViolation of(final Object value,
                                           final @NotNull ConstraintInfo constraintInfo) {
        final @NotNull Object[] arguments = constraintInfo.formatArguments(value);
        String message = constraintInfo.getMessage();
        if (message != null) {
            message = message.replace("%value%", value == null ? "null" : value.toString());
            if (arguments.length == 2) message = message.replace("%expected%", arguments[1].toString());
            else if (arguments.length == 3) message = message
                    .replace("%max%", arguments[1].toString())
                    .replace("%min%", arguments[2].toString());
        }
        String exceptionMessage = String.format(constraintInfo.getExceptionMessage(), arguments);
        return new ConstraintViolation(value, message, exceptionMessage);
    }


}
