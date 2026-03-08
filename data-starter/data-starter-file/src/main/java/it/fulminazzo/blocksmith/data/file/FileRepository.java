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
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (will be used as files names)
 */
public class FileRepository<T, ID> extends AbstractRepository<T, ID, FileQueryEngine<T, ID>> {

    /**
     * Instantiates a new File repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected FileRepository(final @NotNull FileQueryEngine<T, ID> queryEngine,
                             final @NotNull EntityMapper<T, ID> entityMapper) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return queryEngine.query(a -> {
            File file = queryEngine.getDataFile(id);
            if (file.exists()) return Optional.of(a.load(file, entityMapper.getType()));
            else return Optional.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return queryEngine.query(a -> queryEngine.getDataFile(id).exists());
    }

    @Override
    public @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query(a -> {
            saveSingle(a, entity);
            return entity;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return queryEngine.query(a -> {
            File file = queryEngine.getDataFile(id);
            return Files.deleteIfExists(file.toPath());
        });
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
    protected @NotNull CompletableFuture<Collection<T>> findAllImpl(final @NotNull Page page) {
        return queryEngine.query(a -> {
            List<T> result = new ArrayList<>();
            List<File> files = new ArrayList<>(queryEngine.getFiles()).subList(
                    page.getNumber() * page.getSize(),
                    (page.getNumber() + 1) * page.getSize()
            );
            for (File file : files)
                result.add(a.load(file, entityMapper.getType()));
            return result;
        });
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(a -> {
            List<File> files = new ArrayList<>();
            for (ID id : ids)
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
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(a -> {
            for (ID id : ids) {
                File file = queryEngine.getDataFile(id);
                Files.deleteIfExists(file.toPath());
            }
            return null;
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(a -> (long) queryEngine.getFiles().size());
    }

    private void saveSingle(final @NotNull ConfigurationAdapter adapter,
                            final @NotNull T entity) throws IOException {
        ID id = entityMapper.getId(entity);
        File file = queryEngine.getDataFile(id);
        adapter.store(file, entity);
    }

}
