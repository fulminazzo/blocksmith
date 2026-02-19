package it.fulminazzo.blocksmith.data.cache;

import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CachedRepository<T, ID> implements CacheRepository<T, ID> {
    protected final @NotNull EntityMapper<T, ID> entityMapper;

    protected final @NotNull CacheRepository<T, ID> cacheRepository;
    protected final @NotNull Repository<T, ID> repository;

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
        return cacheRepository.existsById(id).thenCompose(r -> {
            if (r) return CompletableFuture.completedFuture(true);
            else return repository.existsById(id);
        });
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
        return cacheRepository.findAllById(ids).thenCompose(r -> {
            List<ID> found = r.stream().map(entityMapper::getId).collect(Collectors.toList());
            List<ID> missing = new ArrayList<>();
            for (ID id : ids)
                if (!found.contains(id)) missing.add(id);
            return repository.findAllById(missing).thenApply(r2 ->
                    Stream.concat(r.stream(), r2.stream()).collect(Collectors.toList())
            );
        });
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entities) {
        return repository.saveAll(entities).thenCompose(e ->
                cacheRepository.saveAll(entities).thenApply(c -> e)
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

    @Override
    public @NotNull CacheRepository<T, ID> setExpiry(final @Range(from = 0, to = Long.MAX_VALUE) long expiry) {
        cacheRepository.setExpiry(expiry);
        return this;
    }

}
