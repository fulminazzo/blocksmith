package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.QueryEngine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * A Query engine with SQL support.
 * <br>
 * Uses the <a href="https://www.jooq.org/">jOOQ</a> library under the hood
 * with custom methods to speed up development.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 * @param <TB> the type of the table containing the entities
 */
//TODO: construction logic
@RequiredArgsConstructor
public final class SqlQueryEngine<T, ID, TB> implements QueryEngine<T, ID> {
    private final @NotNull DSLContext context;
    @Getter
    private final @NotNull TB table;
    @Getter
    private final @NotNull Field<ID> idColumn;
    private final @NotNull Executor executor;

    /**
     * Returns the {@link Condition} that checks if the ID on database equals the one provided.
     *
     * @param id the id
     * @return the condition
     */
    public @NotNull Condition idEquals(final @NotNull ID id) {
        return idColumn.eq(id);
    }

    /**
     * Returns the {@link Condition} that checks if the ID on database equals one of the many provided.
     *
     * @param ids the collection of ids
     * @return the condition
     */
    public @NotNull Condition idIn(final @NotNull Collection<ID> ids) {
        return idColumn.in(ids);
    }

    /**
     * Gets the underlying table for operations that need direct access.
     * Useful for operations where the type needs to be preserved.
     *
     * @return the table where the entities are stored
     */
    public @NotNull Table<?> getWildcardTable() {
        return (Table<?>) table;
    }

    /**
     * Executes multiple SQL queries asynchronously,
     * minimizing the actual queries as much as possible.
     *
     * @param <R>           the type of the query result
     * @param queryFunction the query
     * @return the query result
     */
    public <R> @NotNull CompletableFuture<R> batched(
            final @NotNull BiFunction<DSLContext, Table<?>, R> queryFunction
    ) {
        return CompletableFuture.supplyAsync(
                () -> context.batchedResult(c -> queryFunction.apply(c.dsl(), getWildcardTable())),
                executor
        );
    }

    /**
     * Executes a SQL query asynchronously.
     *
     * @param <R>           the type of the query result
     * @param queryFunction the query
     * @return the query result
     */
    public <R> @NotNull CompletableFuture<R> query(
            final @NotNull BiFunction<DSLContext, Table<?>, R> queryFunction
    ) {
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(context, getWildcardTable()), executor);
    }

}
