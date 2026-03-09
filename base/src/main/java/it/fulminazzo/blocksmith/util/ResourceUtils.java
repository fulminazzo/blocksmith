package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
     * Gets all the resources present in a given directory.
     * <br>
     * <b>NOT RECURSIVE</b> (will NOT check for subdirectories).
     *
     * @param directory the directory (should NOT have a leading "/")
     * @return the resources
     * @throws IOException in case of any errors
     */
    public static @NotNull Collection<String> listResources(final @NotNull String directory) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(directory);
        final URI uri;
        try {
            uri = Objects.requireNonNull(resource, "Could not find resource: " + directory).toURI();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        final Path directoryPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            } catch (FileAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
            }
            directoryPath = fileSystem.getPath(directory);
            fileSystem.close();
        } else directoryPath = Paths.get(uri);

        try (Stream<Path> stream = Files.list(directoryPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }

    }

}
