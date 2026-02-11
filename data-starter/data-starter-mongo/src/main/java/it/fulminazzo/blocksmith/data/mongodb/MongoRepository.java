package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.Function;
import com.mongodb.client.model.CountOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import it.fulminazzo.blocksmith.data.Repository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * A basic implementation of {@link Repository} for MongoDB databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
@RequiredArgsConstructor
public class MongoRepository<T, ID> implements Repository<T, ID> {
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
                collection.insertOne(data)
        ).thenApply(r -> data);
    }

    @Override
    public @NotNull CompletableFuture<?> delete(final @NonNull ID id) {
        return query(collection ->
                collection.deleteOne(eq(idFieldName, id))
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryMany(MongoCollection::find);
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        return queryMany(query -> query.find(in(idFieldName, ids)));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries) {
        return query(collection ->
                collection.insertMany(new ArrayList<>(entries))
        ).thenApply(r -> entries);
    }

    @Override
    public @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids) {
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
