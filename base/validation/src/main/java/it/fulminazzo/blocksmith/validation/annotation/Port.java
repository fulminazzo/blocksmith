package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a numeric parameter or type that represents a networking port.
 * <br>
 * Accepts <code>null</code> values.
 */
@Range(min = 0, max = 65535)
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Port {
    /**
     * The default error message to fall back to in case of violation.
     */
    @NotNull String DEFAULT_MESSAGE = "%1$s is not a valid port";

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.invalid-port";

}
