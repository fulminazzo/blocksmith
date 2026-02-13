package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.QueryEngine;
import it.fulminazzo.blocksmith.function.FunctionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Pseudo-implementation of a Query engine for data files.
 * <br>
 * Although this is not properly linked to a database,
 * the engine still provides a {@link #query(FunctionException)} method for familiarity.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FileQueryEngine<T, ID> implements QueryEngine<T, ID> {
    private final @NotNull ConfigurationAdapter adapter;
    private final @NotNull ConfigurationFormat format;

    private final @NotNull File dataDirectory;
    private final @NotNull Executor executor;

    /**
     * Executes the given function asynchronously.
     *
     * @param <R>           the type of the result
     * @param queryFunction the function
     * @return the result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull FunctionException<ConfigurationAdapter, R, IOException> queryFunction
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return queryFunction.apply(adapter);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Gets all the files of the data.
     *
     * @return the files
     */
    public @NotNull Collection<File> getFiles() {
        if (dataDirectory.exists()) {
            File[] files = dataDirectory.listFiles();
            if (files != null)
                return Arrays.stream(files)
                        .filter(f -> f.getName().endsWith("." + format.getFileExtension()))
                        .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Gets the corresponding data file to the id.
     *
     * @param id the id
     * @return the data file (existence is NOT guaranteed)
     * @throws IOException in case of any exception
     */
    public @NotNull File getDataFile(final @NotNull ID id) throws IOException {
        if (!dataDirectory.isDirectory())
            Files.createDirectories(dataDirectory.toPath());
        return format.getFile(dataDirectory, id.toString());
    }

}
