package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a {@link CharSequence} parameter or type that is a valid HEX color.
 * <br>
 * Accepts {@code null} values.
 * <br>
 * Example usage:
 * <pre>{@code
 * void function(@HexColor String color) {
 *     // color must be a valid HEX color (or null)
 * }
 * }</pre>
 *
 * @see Constraint
 * @see NonNull
 * @see Matches
 */
@Matches("#([0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface HexColor {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     * @see ValidationMessages
     */
    @NotNull String message() default ValidationMessages.REQUIRED_HEX_COLOR;

    /**
     * Gets the error message that will be shown in the
     * {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "'%1$s' is not a valid HEX color";

}
