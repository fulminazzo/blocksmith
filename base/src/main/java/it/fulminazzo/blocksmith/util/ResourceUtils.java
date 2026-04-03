package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

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

    /**
     * Lists all the resources of the current classloader in the specified folder.
     *
     * @param resourceFolder the resources folder
     * @return the list of resources
     * @throws IOException if an error occurs while listing the resources
     */
    public static @NotNull List<String> listResources(final @NotNull String resourceFolder) throws IOException {
        return listResources(resourceFolder, s -> true);
    }

    /**
     * Lists all the resources of the current classloader in the specified folder.
     *
     * @param classLoader    the classloader
     * @param resourceFolder the resources folder
     * @return the list of resources
     * @throws IOException if an error occurs while listing the resources
     */
    public static @NotNull List<String> listResources(final @NotNull ClassLoader classLoader,
                                                      final @NotNull String resourceFolder) throws IOException {
        return listResources(classLoader, resourceFolder, s -> true);
    }

    /**
     * Lists all the resources of the current classloader that match the filter in the specified folder.
     *
     * @param resourceFolder the resources folder
     * @param filter         the filter to apply to the resources
     * @return the list of resources
     * @throws IOException if an error occurs while listing the resources
     */
    public static @NotNull List<String> listResources(final @NotNull String resourceFolder,
                                                      final @NotNull Predicate<String> filter) throws IOException {
        return listResources(classLoader, resourceFolder, filter);
    }

    /**
     * Lists all the resources of the given classloader that match the filter in the specified folder.
     *
     * @param classLoader    the classloader
     * @param resourceFolder the resources folder
     * @param filter         the filter to apply to the resources
     * @return the list of resources
     * @throws IOException if an error occurs while listing the resources
     */
    public static @NotNull List<String> listResources(final @NotNull ClassLoader classLoader,
                                                      final @NotNull String resourceFolder,
                                                      final @NotNull Predicate<String> filter) throws IOException {
        final List<String> results = new ArrayList<>();
        final Enumeration<URL> urls = classLoader.getResources(resourceFolder);
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            if (url.getProtocol().equals("jar")) loadFromJar(url, results, filter);
            else loadFromFileSystem(url, results, filter);
        }
        return results;
    }

    /**
     * Loads all the resources of the given JAR URL that match the filter.
     *
     * @param url     the JAR URL
     * @param results the list to add the results to
     * @param filter  the filter to apply to the resources
     * @throws IOException if an error occurs while loading the resources
     */
    static void loadFromJar(final @NotNull URL url,
                            final @NotNull List<String> results,
                            final @NotNull Predicate<String> filter) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        String entryPrefix = connection.getEntryName();
        try (JarFile jarFile = connection.getJarFile()) {
            jarFile.stream()
                    .filter(e -> !e.isDirectory())
                    .map(ZipEntry::getName)
                    .filter(n -> entryPrefix == null || n.startsWith(entryPrefix + "/"))
                    .filter(filter)
                    .forEach(results::add);
        }
    }

    /**
     * Loads all the files from the given URL that match the filter.
     *
     * @param url     the URL to load from
     * @param results the list to add the results to
     * @param filter  the filter to apply to the files
     * @throws IOException if an error occurs while loading the files
     */
    static void loadFromFileSystem(final @NotNull URL url,
                                   final @NotNull List<String> results,
                                   final @NotNull Predicate<String> filter) throws IOException {
        final Path directory = Path.of(url.getPath());
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .map(directory::relativize)
                    .map(Path::toString)
                    .filter(filter)
                    .forEach(results::add);
        }
    }

    private static @NotNull String getResourceName(final @NotNull String resource) {
        if (resource.contains("/")) return resource.substring(resource.lastIndexOf("/") + 1);
        else return resource;
    }

}
