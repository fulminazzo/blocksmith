package it.fulminazzo.blocksmith.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: proper documentation
 *
 * @see Application
 * @see Initializer
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationExtensionRegistry {
    private static final @NotNull Map<
            Class<? extends Annotation>,
            FieldInitializerHandler
            > HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a new handler for the given annotation.
     *
     * @param annotation the annotation to register the handler for
     * @param handler    the handler to register
     */
    public static void registerHandler(
            final @NotNull Class<? extends Annotation> annotation,
            final @NotNull FieldInitializerHandler handler
    ) {
        HANDLERS.put(annotation, handler);
    }

    /**
     * Gets the handler for the given annotation.
     *
     * @param annotation the annotation to get the handler for
     * @return the handler for the given annotation
     */
    public static @Nullable FieldInitializerHandler getHandler(final @NotNull Class<? extends Annotation> annotation) {
        return HANDLERS.get(annotation);
    }

}
