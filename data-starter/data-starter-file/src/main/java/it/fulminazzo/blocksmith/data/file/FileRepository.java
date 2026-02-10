package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.function.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
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
public class FileRepository<T, ID> implements Repository<T, ID> {
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

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return executeOnSingle(id, f -> {
            if (f.exists()) return Optional.of(adapter.load(f, dataType));
            else return Optional.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return executeOnSingle(id, File::exists);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T data) {
        return executeOnSingleData(data, f -> {
            adapter.store(f, data);
            return data;
        });
    }

    @Override
    public @NotNull CompletableFuture<?> delete(final @NotNull ID id) {
        return executeOnSingle(id, f -> {
            Files.deleteIfExists(f.toPath());
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return executeOnMany(f -> {
            return adapter.load(f, dataType);
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        return executeOnMany(ids, (f, i) -> {
            return adapter.load(f, dataType);
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries) {
        return executeOnManyData(entries, (f, t) -> {
            adapter.store(f, t);
            return t;
        });
    }

    @Override
    public @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids) {
        return executeOnMany(ids, (f, i) -> {
            Files.deleteIfExists(f.toPath());
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return execute(() -> {
            if (workingDir.exists()) {
                File[] files = workingDir.listFiles();
                if (files != null)
                    return Arrays.stream(files)
                            .map(File::getName)
                            .filter(n -> n.endsWith("." + format.getFileExtension()))
                            .count();
            }
            return 0L;
        });
    }

    /*
     * MULTIPLE
     */

    /**
     * Executes the given function on multiple data files.
     *
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnMany(
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return execute(() -> {
            if (workingDir.isDirectory()) {
                File[] files = workingDir.listFiles();
                if (files != null)
                    for (File dataFile : files)
                        function.accept(dataFile);
            }
        });
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
        return execute(() -> {
            final List<R> result = new ArrayList<>();
            if (workingDir.isDirectory()) {
                File[] files = workingDir.listFiles();
                if (files != null)
                    for (File dataFile : files)
                        result.add(function.apply(dataFile));
            }
            return result;
        });
    }

    /*
     * MULTIPLE FILTERED
     */

    /**
     * Executes the given function on multiple data files.
     *
     * @param entries  the entries of the files
     * @param function the function to execute
     * @return the result
     */
    protected @NotNull CompletableFuture<?> executeOnManyData(
            final @NotNull Collection<T> entries,
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return executeOnManyData(entries, (f, t) -> {
            function.accept(f);
        });
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
        return execute(() -> {
            for (T data : entries) {
                ID id = idMapper.apply(data);
                File dataFile = getDataFile(id);
                function.accept(dataFile, data);
            }
        });
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
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        return executeOnManyData(entries, (f, t) -> {
            return function.apply(f);
        });
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
        return execute(() -> {
            final List<R> result = new ArrayList<>();
            for (T data : entries) {
                ID id = idMapper.apply(data);
                File dataFile = getDataFile(id);
                result.add(function.apply(dataFile, data));
            }
            return result;
        });
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
            final @NotNull ConsumerException<File, IOException> function
    ) {
        return executeOnMany(ids, (f, i) -> {
            function.accept(f);
        });
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
        return execute(() -> {
            for (ID id : ids) {
                File dataFile = getDataFile(id);
                function.accept(dataFile, id);
            }
        });
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
            final @NotNull FunctionException<File, R, IOException> function
    ) {
        return executeOnMany(ids, (f, i) -> {
            return function.apply(f);
        });
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
        return execute(() -> {
            final List<R> result = new ArrayList<>();
            for (ID id : ids) {
                File dataFile = getDataFile(id);
                result.add(function.apply(dataFile, id));
            }
            return result;
        });
    }

    /*
     * SINGLE
     */

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
        return execute(() -> function.accept(getDataFile(id)));
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
        return execute(() -> function.apply(getDataFile(id)));
    }

    private @NotNull CompletableFuture<?> execute(
            final @NotNull RunnableException<IOException> function
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                function.run();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    private <R> @NotNull CompletableFuture<R> execute(
            final @NotNull SupplierException<R, IOException> function
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return function.get();
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
