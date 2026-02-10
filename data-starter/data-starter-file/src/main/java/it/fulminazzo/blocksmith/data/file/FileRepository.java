package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.function.ConsumerException;
import it.fulminazzo.blocksmith.function.FunctionException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class FileRepository<ID> {
    private final @NotNull File workingDir;
    private final @NotNull Executor executor;

    /**
     * Executes the given function on a single data file.
     *
     * @param id       the id of the data
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnSingle(
            final @NotNull ID id,
            final @NotNull ConsumerException<File, IOException> function
    ) {
        File dataFile = getDataFile(id);
        return CompletableFuture.runAsync(() -> {
            try {
                function.accept(dataFile);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on a single data file.
     *
     * @param <R>      the type of the result
     * @param id       the id of the data
     * @param function the function to execute
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> executeOnSingle(
            final @NotNull ID id,
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        File dataFile = getDataFile(id);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return function.apply(dataFile);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Gets the corresponding data file to the id.
     *
     * @param id the id
     * @return the data file
     */
    protected @NotNull File getDataFile(final @NotNull ID id) {
        return new File(workingDir, id.toString());
    }

}
