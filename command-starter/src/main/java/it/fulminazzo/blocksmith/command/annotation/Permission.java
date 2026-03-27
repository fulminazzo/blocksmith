package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO: pending documentation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {

    @NotNull String value() default "";

    //TODO: better documentation
    // Identifies all the permissions of the same group, for example
    // "blocksmith.command.reload" and "blocksmith.command.reload.async"
    // are in the group "blocksmith".
    @NotNull String group() default "";

    @NotNull Grant grant() default Grant.OP;

    enum Grant {
        ALL, OP, NONE
    }

}
