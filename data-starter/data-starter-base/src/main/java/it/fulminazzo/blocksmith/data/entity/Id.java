package it.fulminazzo.blocksmith.data.entity;

import java.lang.annotation.*;

/**
 * Indicates that a field identifies the entity where it is contained.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Id {
}
