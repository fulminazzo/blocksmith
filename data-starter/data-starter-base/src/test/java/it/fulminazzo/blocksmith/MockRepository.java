package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Mock {@link it.fulminazzo.blocksmith.data.Repository} for testing purposes.
 */
public final class MockRepository extends AbstractRepository<Cat, String, MockQueryEngine> {

    public MockRepository() {
        super(new MockQueryEngine(), EntityMapper.create(Cat.class, "name"));
    }

    @Override
    public @NotNull CompletableFuture<Optional<Cat>> findById(final @NotNull String s) {
        return queryEngine.query(m -> Optional.ofNullable(m.get(s)));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull String s) {
        return queryEngine.query(m -> m.containsKey(s));
    }

    @Override
    protected @NotNull CompletableFuture<Cat> saveImpl(final @NotNull Cat entity) {
        return queryEngine.query(m -> m.put(entity.getName(), entity));
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull String s) {
        return queryEngine.query(m -> m.remove(s));
    }

    @Override
    public @NotNull CompletableFuture<Collection<Cat>> findAll() {
        return queryEngine.query(Map::values);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<Cat>> findAllByIdImpl(final @NotNull Collection<String> strings) {
        return queryEngine.query(m -> strings.stream()
                .map(m::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<Cat>> saveAllImpl(final @NotNull Collection<Cat> entities) {
        return queryEngine.query(m -> {
            entities.forEach(c -> m.put(c.getName(), c));
            return entities;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<String> strings) {
        return queryEngine.query(m -> {
            strings.forEach(m::remove);
            return null;
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(m -> (long) m.size());
    }

}
