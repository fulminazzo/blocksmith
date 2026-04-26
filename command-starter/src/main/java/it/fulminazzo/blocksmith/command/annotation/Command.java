package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO: pending documentation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command {

    @NotNull String value() default "";

    @NotNull String description() default "";

    boolean dynamic() default false;

    @NotNull Permission permission() default @Permission;

    @NotNull Help help() default @Help;

}
