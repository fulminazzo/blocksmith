package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * A basic implementation of {@link Repository} for MongoDB databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
public class MongoRepository<T, ID> extends AbstractRepository<T, ID> {
    private final @NotNull MongoCollection<T> collection;

    protected MongoRepository(final @NotNull MongoCollection<T> collection,
                              final @NotNull EntityMapper<T, ID> entityMapper) {
        super(entityMapper);
        this.collection = collection;
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NonNull ID id) {
        return query(collection ->
                collection.find(eq(entityMapper.getIdFieldName(), id))
        ).thenApply(Optional::ofNullable);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NonNull ID id) {
        return query(collection ->
                collection.countDocuments(eq(entityMapper.getIdFieldName(), id), new CountOptions().limit(1))
        ).thenApply(c -> c > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NonNull T data) {
        return query(collection ->
                collection.replaceOne(
                        eq(entityMapper.getIdFieldName(), entityMapper.getId(data)),
                        data,
                        new ReplaceOptions().upsert(true)
                )
        ).thenApply(r -> data);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NonNull ID id) {
        return query(collection ->
                collection.deleteOne(eq(entityMapper.getIdFieldName(), id))
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryMany(MongoCollection::find);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryMany(query -> query.find(in(entityMapper.getIdFieldName(), ids)));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entries) {
        List<WriteModel<T>> writeModels = entries.stream()
                .map(e -> new ReplaceOneModel<>(
                        eq(entityMapper.getIdFieldName(), entityMapper.getId(e)),
                        e,
                        new ReplaceOptions().upsert(true)
                ))
                .collect(Collectors.toList());
        return query(collection -> collection.bulkWrite(writeModels))
                .thenApply(result -> entries);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return query(collection ->
                collection.deleteMany(in(entityMapper.getIdFieldName(), ids))
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return query(MongoCollection::countDocuments);
    }

    /**
     * Executes a general query and returns all the results.
     *
     * @param <R>           the type of the results
     * @param queryFunction the query function
     * @return the results
     */
    protected <R> @NotNull CompletableFuture<Collection<R>> queryMany(
            final @NotNull Function<MongoCollection<T>, Publisher<R>> queryFunction
    ) {
        return Flux.from(queryFunction.apply(collection))
                .collectList()
                .toFuture()
                .thenApply(l -> l);
    }

    /**
     * Executes a general query and returns the result.
     *
     * @param <R>           the type of the result
     * @param queryFunction the query
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<MongoCollection<T>, Publisher<R>> queryFunction
    ) {
        return Mono.from(queryFunction.apply(collection)).toFuture();
    }

}
