package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a {@link CharSequence} parameter or type that is usable as an identifier.
 * <br>
 * Accepts <code>null</code> values.
 */
@Matches("[A-Za-z_][A-Za-z0-9_]*")
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Identifier {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.invalid-identifier";

    /**
     * Gets the error message that will be shown in the {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "'%1$s' is not a valid identifier";

}
