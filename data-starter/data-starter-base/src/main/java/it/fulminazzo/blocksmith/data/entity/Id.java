package it.fulminazzo.blocksmith.data.entity;

import java.lang.annotation.*;

/**
 * Marks the field of a class as the identifier of the entity represented by the class itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Id {
}
