package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a Character parameter or type.
 * <br>
 * Accepts {@code null} values.
 */
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Character {

    @NotNull String message() default "error.validation.invalid-character";

    @NotNull String exceptionMessage() default "'%1$s' is not a valid character";

}
