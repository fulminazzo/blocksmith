package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Page;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link Repository} that stores data on disk.
 *
 * @param <T> the type of the entities
 * @param <I> the type of the id of the entities (will be used as files names)
 * @see FileRepositorySettings
 * @see FileQueryEngine
 */
public class FileRepository<T, I> extends AbstractRepository<T, I, FileQueryEngine<T, I>> {

    /**
     * Instantiates a new File repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected FileRepository(
            final @NotNull FileQueryEngine<T, I> queryEngine,
            final @NotNull EntityMapper<T, I> entityMapper
    ) {
        super(queryEngine, entityMapper);
    }

    private void saveSingle(final @NotNull ConfigurationAdapter adapter, final @NotNull T entity) throws IOException {
        I id = entityMapper.getId(entity);
        File file = queryEngine.getDataFile(id);
        adapter.store(file, entity);
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(a -> (long) queryEngine.getFiles().size());
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull I id) {
        return queryEngine.query(a -> {
            File file = queryEngine.getDataFile(id);
            if (file.exists()) return Optional.of(a.load(file, entityMapper.getType()));
            else return Optional.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull I id) {
        return queryEngine.query(a -> queryEngine.getDataFile(id).exists());
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.query(a -> {
            List<T> result = new ArrayList<>();
            for (File file : queryEngine.getFiles())
                result.add(a.load(file, entityMapper.getType()));
            return result;
        });
    }

    @Override
    protected @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query(a -> {
            saveSingle(a, entity);
            return entity;
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllImpl(final @NotNull Page page) {
        return queryEngine.query(a -> {
            List<T> result = new ArrayList<>();
            int from = page.getNumber() * page.getSize();
            int to = (page.getNumber() + 1) * page.getSize();
            Collection<File> files = queryEngine.getFiles();
            files = new ArrayList<>(files).subList(
                    Math.min(files.size(), from),
                    Math.min(files.size(), to)
            );
            for (File file : files)
                result.add(a.load(file, entityMapper.getType()));
            return result;
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<I> ids) {
        return queryEngine.query(a -> {
            List<File> files = new ArrayList<>();
            for (I id : ids)
                files.add(queryEngine.getDataFile(id));
            List<T> result = new ArrayList<>();
            for (File file : files)
                if (file.exists())
                    result.add(a.load(file, entityMapper.getType()));
            return result;
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return queryEngine.query(a -> {
            for (T e : entities) saveSingle(a, e);
            return entities;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<I> ids) {
        return queryEngine.query(a -> {
            for (I id : ids) {
                File file = queryEngine.getDataFile(id);
                Files.deleteIfExists(file.toPath());
            }
            return null;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull I id) {
        return queryEngine.query(a -> {
            File file = queryEngine.getDataFile(id);
            return Files.deleteIfExists(file.toPath());
        });
    }

}
