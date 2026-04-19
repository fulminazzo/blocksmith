package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a numeric parameter or type that must negative (zero not allowed).
 * <br>
 * Also supports {@link java.time.Duration}, where the milliseconds are compared.
 * <br>
 * Accepts {@code null} values.
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Negative {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default ValidationMessages.REQUIRED_NEGATIVE;

    /**
     * Gets the error message that will be shown in the {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "must be negative";

}
