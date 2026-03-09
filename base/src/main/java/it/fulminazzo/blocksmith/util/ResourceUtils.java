package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of utilities to work with resources of the JDK.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtils {

    /**
     * Stores the requested resource on disk.
     *
     * @param resource the resource name (should NOT have a preceding "/")
     * @param file     the file where it will be stored
     * @throws IOException in case of any errors
     */
    public static void storeResource(final @NotNull String resource,
                                     final @NotNull File file) throws IOException {
        Files.createDirectories(file.toPath().getParent());
        Files.createFile(file.toPath());
        try (InputStream input = ResourceUtils.class.getClassLoader().getResourceAsStream(resource);
             OutputStream output = new FileOutputStream(file)) {
            if (input == null)
                throw new IllegalArgumentException("Could not find resource: " + resource);
            input.transferTo(output);
        }
    }

    /**
     * Gets all the resources present in a given directory.
     * <br>
     * <b>NOT RECURSIVE</b> (will NOT check for subdirectories).
     *
     * @param directory the directory name (should NOT have a preceding "/")
     * @return the resources
     * @throws IOException in case of any errors
     */
    public static @NotNull Collection<String> listResources(final @NotNull String directory) throws IOException {
        URL resource = ResourceUtils.class.getClassLoader().getResource(directory);
        final URI uri;
        try {
            uri = Objects.requireNonNull(resource, "Could not find resource: " + directory).toURI();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        final Path directoryPath;
        FileSystem fileSystem = null;
        if (uri.getScheme().equals("jar")) {
            try {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            } catch (FileAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
            }
            directoryPath = fileSystem.getPath(directory);
        } else directoryPath = Paths.get(uri);

        try (Stream<Path> stream = Files.list(directoryPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } finally {
            if (fileSystem != null) fileSystem.close();
        }
    }
}
