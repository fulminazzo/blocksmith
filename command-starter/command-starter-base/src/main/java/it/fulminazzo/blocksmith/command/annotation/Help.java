package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO: pending documentation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Help {
    @NotNull String DEFAULT_NAME = "help";

    @NotNull String @NotNull [] aliases() default {DEFAULT_NAME};

    @NotNull String description() default "";

    @NotNull Permission permission() default @Permission;

}
