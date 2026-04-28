package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.Cat;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Mock {@link QueryEngine} for testing purposes.
 */
public class MockQueryEngine implements QueryEngine<Cat, String> {
    private final @NotNull Map<String, Cat> map = new ConcurrentHashMap<>();

    /**
     * Executes a mock query asynchronously.
     *
     * @param <R>           the type of the query result
     * @param queryFunction the query
     * @return the query result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull Function<Map<String, Cat>, R> queryFunction
    ) {
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(map));
    }

}
