package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a character parameter or type that must be contained between two values.
 * <br>
 * Also supports {@link java.time.Duration}, where the milliseconds are compared.
 * <br>
 * Accepts <code>null</code> values.
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface RangeChar {

    /**
     * The minimum value allowed.
     *
     * @return the value
     */
    char min();

    /**
     * The maximum value allowed.
     *
     * @return the value
     */
    char max();

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.character-exceeds-range";

    /**
     * Gets the error message that will be shown in the {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "must be at least '%3$s' and at most '%2$s'";

}
