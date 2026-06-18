package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Handles a specific annotation on a field in a {@link Application} instance.
 *
 * @param <A> the type of the annotation
 * @see Application
 * @see ApplicationLoader
 */
@FunctionalInterface
public interface FieldAnnotationHandler<A extends Annotation> {

    /**
     * Handles the annotation on the given field.
     *
     * @param application the application (where the field should reside on)
     * @param annotation the annotation
     * @param field the field in the application
     * @param environment a map containing any result of previous computations
     * @return the result of the computation (can be {@code null})
     */
    Object handle(
            final @NotNull Application application,
            final @NotNull A annotation,
            final @NotNull Field field,
            final @NotNull Map<@NotNull String, Object> environment
    );

}
