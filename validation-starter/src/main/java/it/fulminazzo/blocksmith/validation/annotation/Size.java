package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a general parameter or type that has a length or size.
 * Supported types are {@link CharSequence}, arrays, {@link java.util.Collection} instances,
 * {@link java.util.Map} instances and anything with either a {@code length()}
 * or {@code size()} method.
 * <br>
 * Accepts {@code null} values.
 * <br>
 * Example usage:
 * <pre>{@code
 * void function(@Size(min = 1, max = 10) List<String> items) {
 *     // items must have between 1 and 10 elements (or null)
 * }
 * }</pre>
 *
 * @see Constraint
 * @see NonNull
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Size {

    /**
     * The minimum value allowed.
     *
     * @return the value
     */
    int min();

    /**
     * The maximum value allowed.
     *
     * @return the value
     */
    int max();

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     * @see ValidationMessages
     */
    @NotNull String message() default ValidationMessages.EXCEEDS_SIZE;

    /**
     * Gets the error message that will be shown in the
     * {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "size must be at least %3$s and at most %2$s elements long";

}
