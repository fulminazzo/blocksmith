package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A special implementation of {@link Repository} that supports internal caching.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (will be used as files names)
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CachedRepository<T, ID> implements Repository<T, ID> {
    protected final @NotNull CacheRepository<T, ID> cacheRepository;
    protected final @NotNull Repository<T, ID> repository;

    protected final @NotNull EntityMapper<T, ID> entityMapper;

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return cacheRepository.findById(id).thenCompose(r -> {
            if (r.isPresent()) return CompletableFuture.completedFuture(r);
            else return repository.findById(id)
                    .thenCompose(o -> {
                        if (o.isPresent()) return cacheRepository.save(o.get()).thenApply(s -> o);
                        else return CompletableFuture.completedFuture(o);
                    });
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return findById(id).thenApply(Optional::isPresent);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        return repository.save(entity).thenCompose(e ->
                cacheRepository.save(e).thenApply(c -> e)
        );
    }

    @Override
    public @NotNull CompletableFuture<Void> delete(final @NotNull ID id) {
        return CompletableFuture.allOf(
                cacheRepository.delete(id),
                repository.delete(id)
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return repository.findAll();
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        return cacheRepository.findAllById(ids).thenCompose(c -> {
            List<ID> found = c.stream().map(entityMapper::getId).collect(Collectors.toList());
            Collection<ID> missing = ids.stream()
                    .filter(i -> !found.contains(i))
                    .collect(Collectors.toList());
            if (missing.isEmpty()) return CompletableFuture.completedFuture(c);
            else return repository.findAllById(missing).thenCompose(r -> {
                if (r.isEmpty()) return CompletableFuture.completedFuture(c);
                else return cacheRepository.saveAll(r).thenApply(s ->
                        Stream.concat(c.stream(), r.stream()).collect(Collectors.toList())
                );
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities) {
        return repository.saveAll(entities).thenCompose(e ->
                cacheRepository.saveAll(e).thenApply(c -> e)
        );
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteAll(final @NotNull Collection<ID> ids) {
        return CompletableFuture.allOf(
                cacheRepository.deleteAll(ids),
                repository.deleteAll(ids)
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return repository.count();
    }

}
