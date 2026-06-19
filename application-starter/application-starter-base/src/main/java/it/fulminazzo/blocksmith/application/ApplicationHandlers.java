package it.fulminazzo.blocksmith.application;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows registration of all the handlers for the startup and shutdown of a {@link Application}.
 *
 * @see Application
 * @see FieldAnnotationHandler
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ApplicationHandlers {
    private static final @NotNull Map<
            Class<? extends Annotation>,
            FieldAnnotationHandler<?>
            > FIELD_ANNOTATION_HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a new {@link FieldAnnotationHandler} for the given annotation class.
     *
     * @param <A>             the type of the annotation
     * @param annotationClass the annotation class to handle
     * @param handler         the handler to use
     * @see FieldAnnotationHandler
     */
    public static <A extends Annotation> void registerFieldAnnotationHandler(
            final @NotNull Class<A> annotationClass,
            final @NotNull FieldAnnotationHandler<A> handler
    ) {
        FIELD_ANNOTATION_HANDLERS.put(annotationClass, handler);
    }

    /**
     * Gets the {@link FieldAnnotationHandler} for the given annotation class.
     *
     * @param annotationClass the annotation class
     * @return the field annotation handler (if found)
     */
    static @NotNull Optional<FieldAnnotationHandler<?>> getFieldAnnotationHandler(
            final @NotNull Class<? extends Annotation> annotationClass
    ) {
        return Optional.ofNullable(FIELD_ANNOTATION_HANDLERS.get(annotationClass));
    }

}
