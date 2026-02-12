package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.reactivestreams.client.MongoCollection;
import it.fulminazzo.blocksmith.data.QueryEngine;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A query engine with MongoDB support.
 * <br>
 * Uses the official
 * <a href="https://www.mongodb.com/docs/languages/java/reactive-streams-driver/current/getting-started/">MongoDB Reactive Streams</a>
 * library under the hood to leverage the speed and optimizations provided by Netty asynchronous operations.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
//TODO: construction logic
@RequiredArgsConstructor
public final class MongoQueryEngine<T, ID> implements QueryEngine<T, ID> {
    private final @NotNull MongoCollection<T> collection;

    /**
     * Executes a MongoDB query asynchronously that returns a collection of values.
     *
     * @param <R>           the type of the query results
     * @param queryFunction the query
     * @return the query results
     */
    public <R> @NotNull CompletableFuture<Collection<R>> queryMany(
            final @NotNull Function<MongoCollection<T>, Publisher<R>> queryFunction
    ) {
        return Flux.from(queryFunction.apply(collection))
                .collectList()
                .toFuture()
                .thenApply(l -> l);
    }

    /**
     * Executes a MongoDB query asynchronously.
     *
     * @param <R>           the type of the query result
     * @param queryFunction the query
     * @return the query result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<MongoCollection<T>, Publisher<R>> queryFunction
    ) {
        return Mono.from(queryFunction.apply(collection)).toFuture();
    }

}
