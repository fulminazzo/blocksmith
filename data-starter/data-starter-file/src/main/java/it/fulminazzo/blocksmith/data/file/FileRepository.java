package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.function.BiConsumerException;
import it.fulminazzo.blocksmith.function.BiFunctionException;
import it.fulminazzo.blocksmith.function.ConsumerException;
import it.fulminazzo.blocksmith.function.FunctionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A basic implementation of {@link Repository} that stores data on disk.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (will be used as file names)
 */
public class FileRepository<T, ID> {
    protected final @NotNull ConfigurationAdapter adapter;
    private final @NotNull File workingDir;
    private final @NotNull Function<T, ID> idMapper;
    private final @NotNull Class<T> dataType;
    private final @NotNull Executor executor;
    private final @NotNull ConfigurationFormat format;

    /**
     * Instantiates a new File repository.
     *
     * @param workingDir the directory where the data will be stored
     * @param dataType   the type of the data
     * @param idMapper   the function to get the id from data
     * @param executor   the executor
     * @param logger     the logger
     * @param format     the file format to use
     */
    protected FileRepository(final @NotNull File workingDir,
                             final @NotNull Class<T> dataType,
                             final @NotNull Function<T, ID> idMapper,
                             final @NotNull Executor executor,
                             final @NotNull Logger logger,
                             final @NotNull ConfigurationFormat format) {
        this.adapter = ConfigurationAdapter.newAdapter(logger, format);
        this.workingDir = workingDir;
        this.idMapper = idMapper;
        this.dataType = dataType;
        this.executor = executor;
        this.format = format;
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param entries  the entries of the files
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnManyData(
            final @NotNull Collection<T> entries,
            final @NotNull BiConsumerException<File, T, IOException> function
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (T data : entries) {
                    ID id = idMapper.apply(data);
                    File dataFile = getDataFile(id);
                    function.accept(dataFile, data);
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param <R>      the type of the result
     * @param entries  the entries of the files
     * @param function the function to execute
     * @return a collection containing the results for each data
     */
    protected <R> @NotNull CompletableFuture<Collection<R>> executeOnManyData(
            final @NotNull Collection<T> entries,
            final @NotNull BiFunctionException<File, T, R, IOException> function
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final List<R> result = new ArrayList<>();
                for (T data : entries) {
                    ID id = idMapper.apply(data);
                    File dataFile = getDataFile(id);
                    result.add(function.apply(dataFile, data));
                }
                return result;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on a single data file.
     *
     * @param data     the data of the file
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnSingleData(
            final @NotNull T data,
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return executeOnSingle(
                idMapper.apply(data),
                function
        );
    }

    /**
     * Executes the given function on a single data file.
     *
     * @param <R>      the type of the result
     * @param data     the data of the file
     * @param function the function to execute
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> executeOnSingleData(
            final @NotNull T data,
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        return executeOnSingle(
                idMapper.apply(data),
                function
        );
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnMany(
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (workingDir.isDirectory()) {
                    File[] files = workingDir.listFiles();
                    if (files != null)
                        for (File dataFile : files)
                            function.accept(dataFile);
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param <R>      the type of the result
     * @param function the function to execute
     * @return a collection containing the results for each id
     */
    protected <R> @NotNull CompletableFuture<Collection<R>> executeOnMany(
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final List<R> result = new ArrayList<>();
                if (workingDir.isDirectory()) {
                    File[] files = workingDir.listFiles();
                    if (files != null)
                        for (File dataFile : files)
                            result.add(function.apply(dataFile));
                }
                return result;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param ids      the ids of the files
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnMany(
            final @NotNull Collection<ID> ids,
            final @NotNull BiConsumerException<File, ID, IOException> function
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (ID id : ids) {
                    File dataFile = getDataFile(id);
                    function.accept(dataFile, id);
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on multiple data files.
     *
     * @param <R>      the type of the result
     * @param ids      the ids of the files
     * @param function the function to execute
     * @return a collection containing the results for each id
     */
    protected <R> @NotNull CompletableFuture<Collection<R>> executeOnMany(
            final @NotNull Collection<ID> ids,
            final @NotNull BiFunctionException<File, ID, R, IOException> function
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final List<R> result = new ArrayList<>();
                for (ID id : ids) {
                    File dataFile = getDataFile(id);
                    result.add(function.apply(dataFile, id));
                }
                return result;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes the given function on a single data file.
     *
     * @param id       the id of the file
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnSingle(
            final @NotNull ID id,
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                File dataFile = getDataFile(id);
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
     * @param id       the id of the file
     * @param function the function to execute
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> executeOnSingle(
            final @NotNull ID id,
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File dataFile = getDataFile(id);
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
     * @throws IOException in case of any exception
     */
    protected @NotNull File getDataFile(final @NotNull ID id) throws IOException {
        if (!workingDir.isDirectory())
            Files.createDirectories(workingDir.toPath());
        return format.getFile(workingDir, id.toString());
    }

}
