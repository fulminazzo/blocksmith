package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a numeric parameter or type that must be contained between two values.
 * <br>
 * Also supports {@link java.time.Duration}, where the milliseconds are compared.
 * <br>
 * Accepts {@code null} values.
 * <br>
 * Example usage:
 * <pre>{@code
 * void function(@Range(min = 0, max = 100) int percentage) {
 *     // percentage must be between 0 and 100 (or null)
 * }
 * }</pre>
 * <pre>{@code
 * void function(@Range(min = 1000, max = 30000) Duration timeout) {
 *     // timeout must be between 1000 and 30000 milliseconds (or null)
 * }
 * }</pre>
 *
 * @see Constraint
 * @see NonNull
 * @see Min
 * @see Max
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Range {

    /**
     * The minimum value allowed.
     *
     * @return the value
     */
    double min();

    /**
     * The maximum value allowed.
     *
     * @return the value
     */
    double max();

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     * @see ValidationMessages
     */
    @NotNull String message() default ValidationMessages.NUMBER_EXCEEDS_RANGE;

    /**
     * Gets the error message that will be shown in the
     * {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "must be at least %3$s and at most %2$s";

}
