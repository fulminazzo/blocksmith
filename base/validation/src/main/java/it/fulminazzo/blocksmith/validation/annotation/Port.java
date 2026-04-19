package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a numeric parameter or type that represents a networking port.
 * <br>
 * Accepts {@code null} values.
 * <br>
 * <b>WARNING</b>: does not support {@code 0} as port.
 */
@Range(min = 1, max = 65535)
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Port {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default ValidationMessages.REQUIRED_PORT;

    /**
     * Gets the error message that will be shown in the {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "%1$s is not a valid port";

}
