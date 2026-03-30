package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a general parameter or type that has a length or size.
 * Supported types are {@link CharSequence}, arrays, {@link java.util.Collection} instances
 * and anything with either a <code>length</code> or <code>size()</code> method.
 * <br>
 * Accepts <code>null</code> values.
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Size {
    /**
     * The default error message to fall back to in case of violation.
     */
    @NotNull String DEFAULT_MESSAGE = "%1$s size must be at least %3$s and at most %2$s";

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
     */
    @NotNull String message() default "error.validation.argument-exceeds-size";

}
