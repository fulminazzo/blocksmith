package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.ValidationMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a time parameter or type that
 * must be after the current time of execution (milliseconds are ignored).
 * <br>
 * Supported types are:
 * <ul>
 *     <li>{@link java.util.Date};</li>
 *     <li>{@link java.util.Calendar};</li>
 *     <li>any {@link java.time.temporal.TemporalAccessor} implementation, like
 *     {@link java.time.Instant}, {@link java.time.LocalTime} or {@link java.time.LocalDate}.</li>
 * </ul>
 * <br>
 * Accepts {@code null} values.
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface After {

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default ValidationMessages.REQUIRED_AFTER_NOW;

    /**
     * Gets the error message that will be shown in the {@link it.fulminazzo.blocksmith.validation.ValidationException} message.
     *
     * @return the message
     */
    @NotNull String exceptionMessage() default "must be after now";

}
