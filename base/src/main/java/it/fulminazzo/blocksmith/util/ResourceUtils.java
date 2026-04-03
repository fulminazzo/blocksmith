package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
     * @param resource    the resource (should NOT have a preceding "/")
     * @return the resource
     * @throws IllegalArgumentException if the resource was not found
     */
    public static @NotNull InputStream getResource(final @NotNull ClassLoader classLoader, final @NotNull String resource) {
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        if (inputStream == null)
            throw new IllegalArgumentException(String.format("Could not find resource '%s' from classloader", resource));
        return inputStream;
    }

    /**
     * Extracts a resource to the given directory (only if it is not previously existing).
     *
     * @param resource  the resource (should NOT have a preceding "/")
     * @param directory the directory to extract to
     * @return the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull File extractIfAbsent(final @NotNull String resource, final @NotNull File directory) throws IOException {
        return extractIfAbsent(resource, directory.toPath()).toFile();
    }

    /**
     * Extracts a resource to the given directory (only if it is not previously existing).
     *
     * @param classLoader the classloader
     * @param resource    the resource (should NOT have a preceding "/")
     * @param directory   the directory to extract to
     * @return the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static File extractIfAbsent(final @NotNull ClassLoader classLoader,
                                       final @NotNull String resource,
                                       final @NotNull File directory) throws IOException {
        return extractIfAbsent(classLoader, resource, directory.toPath()).toFile();
    }

    /**
     * Extracts a resource to the given directory (only if it is not previously existing).
     *
     * @param resource  the resource (should NOT have a preceding "/")
     * @param directory the directory to extract to
     * @return the path to the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull Path extractIfAbsent(final @NotNull String resource, final @NotNull Path directory) throws IOException {
        return extractIfAbsent(classLoader, resource, directory);
    }

    /**
     * Extracts a resource to the given directory (only if it is not previously existing).
     *
     * @param classLoader the classloader
     * @param resource    the resource (should NOT have a preceding "/")
     * @param directory   the directory to extract to
     * @return the path to the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull Path extractIfAbsent(final @NotNull ClassLoader classLoader,
                                                final @NotNull String resource,
                                                final @NotNull Path directory) throws IOException {
        Path target = directory.resolve(getResourceName(resource));
        if (Files.exists(target)) return target;
        else return extract(classLoader, resource, directory);
    }

    /**
     * Extracts a resource to the given directory.
     *
     * @param resource  the resource (should NOT have a preceding "/")
     * @param directory the directory to extract to
     * @return the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull File extract(final @NotNull String resource, final @NotNull File directory) throws IOException {
        return extract(resource, directory.toPath()).toFile();
    }

    /**
     * Extracts a resource to the given directory.
     *
     * @param classLoader the classloader
     * @param resource    the resource (should NOT have a preceding "/")
     * @param directory   the directory to extract to
     * @return the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static File extract(final @NotNull ClassLoader classLoader,
                               final @NotNull String resource,
                               final @NotNull File directory) throws IOException {
        return extract(classLoader, resource, directory.toPath()).toFile();
    }

    /**
     * Extracts a resource to the given directory.
     *
     * @param resource  the resource (should NOT have a preceding "/")
     * @param directory the directory to extract to
     * @return the path to the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull Path extract(final @NotNull String resource, final @NotNull Path directory) throws IOException {
        return extract(classLoader, resource, directory);
    }

    /**
     * Extracts a resource to the given directory.
     *
     * @param classLoader the classloader
     * @param resource    the resource (should NOT have a preceding "/")
     * @param directory   the directory to extract to
     * @return the path to the extracted resource
     * @throws IOException if an error occurs while extracting the resource
     */
    public static @NotNull Path extract(final @NotNull ClassLoader classLoader,
                                        final @NotNull String resource,
                                        final @NotNull Path directory) throws IOException {
        Files.createDirectories(directory);
        String fileName = getResourceName(resource);
        try (InputStream stream = getResource(classLoader, resource)) {
            Path path = directory.resolve(fileName);
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        }
    }

    private static @NotNull String getResourceName(final @NotNull String resource) {
        if (resource.contains("/")) return resource.substring(resource.lastIndexOf("/") + 1);
        else return resource;
    }

}
