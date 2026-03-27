package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

//TODO: pending documentation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Confirm {

    long timeout() default 10;

    @NotNull TimeUnit unit() default TimeUnit.SECONDS;

}
