package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a string parameter or type that represents an IPv6.
 * <br>
 * Accepts <code>null</code> values.
 */
@Matches("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|(([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?::(([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?)")
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface IPv6 {
    /**
     * The default error message to fall back to in case of violation.
     */
    @NotNull String DEFAULT_MESSAGE = "'%1$s' is not a valid IPv6";

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.invalid-ipv6";

}
