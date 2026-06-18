package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A loader for {@link Application} instances.
 *
 * @see Application
 * @see BlocksmithApplication
 * @see FieldAnnotationHandler
 */
public final class ApplicationLoader {
    private static final @NotNull Map<
            Class<? extends Annotation>,
            FieldAnnotationHandler<?>
            > FIELD_ANNOTATION_HANDLERS = new ConcurrentHashMap<>();

    /**
     * Register a new field annotation handler.
     *
     * @param <A>             the type of the annotation
     * @param annotationClass the annotation class to handle
     * @param handler         the handler to use
     */
    public static <A extends Annotation> void registerFieldAnnotationHandler(
            final @NotNull Class<A> annotationClass,
            final @NotNull FieldAnnotationHandler<A> handler
    ) {
        FIELD_ANNOTATION_HANDLERS.put(annotationClass, handler);
    }

}
