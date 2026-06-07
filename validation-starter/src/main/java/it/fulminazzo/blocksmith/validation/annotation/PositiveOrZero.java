package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a numeric parameter or type that must positive or at most zero.
 * <br>
 * Also supports {@link java.time.Duration}, where the milliseconds are compared.
 * <br>
 * Accepts {@code null} values.
 * <br>
 * Example usage:
 * <pre>{@code
 * void function(@PositiveOrZero int count) {
 *     // count must be positive or zero (or null)
 * }
 * }</pre>
 * <pre>{@code
 * void function(@PositiveOrZero Duration duration) {
 *     // duration must be positive or zero in milliseconds (or null)
 * }
 * }</pre>
 *
 * @see Constraint
 * @see NonNull
 * @see Min
 * @see Positive
 * @see Negative
 * @see NegativeOrZero
 */
@Min(0)
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface PositiveOrZero {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     * @see ValidationMessages
     */
    @NotNull String message() default ValidationMessages.REQUIRED_POSITIVE_OR_ZERO;

    /**
     * Gets the error message that will be shown in the
     * {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "must be positive or zero";

}
