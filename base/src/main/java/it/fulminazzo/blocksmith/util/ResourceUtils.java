package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * A collection of utilities to work with resources.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtils {
    private static final @NotNull ClassLoader classLoader = ResourceUtils.class.getClassLoader();

    /**
     * Gets a resource from the current classloader.
     *
     * @param resource the resource (should NOT have a preceding "/")
     * @return the resource
     * @throws IllegalArgumentException if the resource was not found
     */
    public static @NotNull InputStream getResource(final @NotNull String resource) {
        return getResource(classLoader, resource);
    }

    /**
     * Gets a resource from the given classloader.
     *
     * @param classLoader the classloader
     * @param resource the resource (should NOT have a preceding "/")
     * @return the resource
     * @throws IllegalArgumentException if the resource was not found
     */
    public static @NotNull InputStream getResource(final @NotNull ClassLoader classLoader, final @NotNull String resource) {
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        if (inputStream == null)
            throw new IllegalArgumentException(String.format("Could not find resource '%s' from classloader", resource));
        return inputStream;
    }

}
