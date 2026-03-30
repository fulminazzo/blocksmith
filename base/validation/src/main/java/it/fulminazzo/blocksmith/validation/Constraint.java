package it.fulminazzo.blocksmith.validation;

import java.lang.annotation.*;

/**
 * Identifies an annotation that will be considered as a constraint.
 * <br>
 * Annotations lacking this annotation will not be validated from {@link Validator}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Constraint {
}
