package it.fulminazzo.blocksmith.validation.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a parameter or type that must be <code>false</code>.
 * <br>
 * Accepts <code>null</code> values.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface AssertFalse {
    /**
     * The default error message to fall back to in case of violation.
     */
    @NotNull String DEFAULT_MESSAGE = "'%s' must be false";

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.required-false";

}
