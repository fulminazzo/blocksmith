package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Functional interface for handling a specific annotation on a field in a {@link Application} instance.
 * <br>
 * The annotation has only one requirement: its {@link Retention} must be {@code RUNTIME},
 * otherwise it will not be processed.
 * <br>
 * To create interactions between fields, it is possible to specify the special value {@code dependsOn}
 * as a String or String array in the annotation.
 * Doing so ensures that, during the {@link Application} initialization, any specified field is loaded
 * <b>before</b> the one the annotation is put on.
 * <br>
 * For example:
 * <pre>{@code
 * public class MyApplication extends Application {
 *
 *     @Configuration
 *     public ConfigBean configuration;
 *
 *     @Database(dependsOn = "configuration")
 *     public DataSource dataSource;
 *
 * }
 * }</pre>
 * When the application enables, when the handler for {@code Database} is called,
 * the {@code configuration} field will already be loaded and retrievable from the environment.
 * <br>
 * <b>NOTE</b>: a handler must be registered for both annotations.
 * <br>
 * To be even more precise, one could do:
 * <pre>{@code
 * public class MyApplication extends Application {
 *
 *     @Configuration
 *     public ConfigBean configuration;
 *
 *     @Database(dependsOn = "configuration.databaseConfiguration")
 *     public DataSource dataSource;
 *
 * }
 * }</pre>
 * This code will ensure that a {@code databaseConfiguration} field is located inside of {@code ConfigBean}
 * and it is passed to the handler directly.
 * <b>WARNING</b>: it is <b>not guaranteed</b> by default that the subfield is not {@code null}.
 *
 * @param <A> the type of the annotation
 * @see Application
 * @see ApplicationHandlers
 */
@FunctionalInterface
public interface FieldAnnotationHandler<A extends Annotation> {

    /**
     * Handles the annotation on the given field.
     *
     * @param application the application (where the field should reside on)
     * @param annotation  the annotation
     * @param field       the field in the application
     * @param environment a map containing any result of previous computations
     * @return the value to be set on the field
     * @throws ApplicationEnableException if an error occurs during the processing
     */
    Object handle(
            final @NotNull Application application,
            final @NotNull A annotation,
            final @NotNull Field field,
            final @NotNull Map<@NotNull String, Object> environment
    ) throws ApplicationEnableException;

}
