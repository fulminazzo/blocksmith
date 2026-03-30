package it.fulminazzo.blocksmith.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import it.fulminazzo.blocksmith.validation.annotation.Matches;

import java.lang.annotation.*;

/**
 * Identifies a {@link CharSequence} parameter or type that represents a UUID.
 * <br>
 * Accepts <code>null</code> values.
 */
@Matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Uuid {
    // represents a custom annotation with no message methods

}
