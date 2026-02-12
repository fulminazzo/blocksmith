package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MongoRepository<T, ID> extends AbstractRepository<T, ID> {
    private final @NotNull MongoCollection<T> collection;
    private final @NotNull String idFieldName;
    private final @NotNull Function<T, ID> idMapper;

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NonNull ID id) {
        return query(collection ->
                collection.find(eq(idFieldName, id))
        ).thenApply(Optional::ofNullable);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NonNull ID id) {
        return query(collection ->
                collection.countDocuments(eq(idFieldName, id), new CountOptions().limit(1))
        ).thenApply(c -> c > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NonNull T data) {
        return query(collection ->
                collection.replaceOne(
                        eq(idFieldName, idMapper.apply(data)),
                        data,
                        new ReplaceOptions().upsert(true)
                )
        ).thenApply(r -> data);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NonNull ID id) {
        return query(collection ->
                collection.deleteOne(eq(idFieldName, id))
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryMany(MongoCollection::find);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryMany(query -> query.find(in(idFieldName, ids)));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entries) {
        List<WriteModel<T>> writeModels = entries.stream()
                .map(e -> new ReplaceOneModel<>(
                        eq(idFieldName, idMapper.apply(e)),
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
                collection.deleteMany(in(idFieldName, ids))
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
