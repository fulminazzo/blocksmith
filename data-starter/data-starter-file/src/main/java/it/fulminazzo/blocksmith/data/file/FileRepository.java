package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
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
import java.util.stream.Collectors;

/**
 * A basic implementation of {@link Repository} that stores data on disk.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (will be used as file names)
 */
public class FileRepository<T, ID> extends AbstractRepository<T, ID> {
    protected final @NotNull ConfigurationAdapter adapter;
    private final @NotNull File dataDirectory;
    private final @NotNull Executor executor;
    private final @NotNull ConfigurationFormat format;

    protected FileRepository(final @NotNull File dataDirectory,
                             final @NotNull EntityMapper<T, ID> entityMapper,
                             final @NotNull Executor executor,
                             final @NotNull Logger logger,
                             final @NotNull ConfigurationFormat format) {
        super(entityMapper);
        this.adapter = ConfigurationAdapter.newAdapter(logger, format);
        this.dataDirectory = dataDirectory;
        this.executor = executor;
        this.format = format;
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return executeOnSingle(id, f -> {
            if (f.exists()) return Optional.of(adapter.load(f, entityMapper.getType()));
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
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return executeOnSingle(id, f -> {
            Files.deleteIfExists(f.toPath());
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return executeOnMany(f -> {
            return adapter.load(f, entityMapper.getType());
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return executeOnMany(ids, (f, i) -> {
            return adapter.load(f, entityMapper.getType());
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entries) {
        return executeOnManyData(entries, (f, t) -> {
            adapter.store(f, t);
            return t;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return executeOnMany(ids, (f, i) -> {
            Files.deleteIfExists(f.toPath());
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return execute(() -> (long) getFiles().size());
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
            for (File dataFile : getFiles()) function.accept(dataFile);
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
            for (File dataFile : getFiles()) result.add(function.apply(dataFile));
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
                ID id = entityMapper.getId(data);
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
                ID id = entityMapper.getId(data);
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
                entityMapper.getId(data),
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
                entityMapper.getId(data),
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
     * Gets all the files of the data.
     *
     * @return the files
     */
    protected @NotNull Collection<File> getFiles() {
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
     * @return the data file
     * @throws IOException in case of any exception
     */
    protected @NotNull File getDataFile(final @NotNull ID id) throws IOException {
        if (!dataDirectory.isDirectory())
            Files.createDirectories(dataDirectory.toPath());
        return format.getFile(dataDirectory, id.toString());
    }

    /**
     * Gets a new builder for this class.
     *
     * @param <T>      the type of the data
     * @param dataType the data type
     * @return the builder
     */
    public static <T> @NotNull FileRepositoryBuilder<T> builder(final @NotNull Class<T> dataType) {
        return new FileRepositoryBuilder<>(dataType);
    }

}
