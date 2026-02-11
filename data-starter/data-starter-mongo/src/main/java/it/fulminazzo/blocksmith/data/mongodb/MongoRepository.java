package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.Function;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MongoRepository<T, ID> {
    private final @NotNull MongoCollection<T> collection;
    private final @NotNull String idFieldName;
    private final @NotNull Function<T, ID> idMapper;

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
