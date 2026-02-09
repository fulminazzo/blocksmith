package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.Repository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A basic implementation of {@link Repository} for SQL databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
@SuppressWarnings({"resource"})
@RequiredArgsConstructor
public class SqlRepository<T, ID> implements Repository<T, ID> {
    private final @NotNull DSLContext context;
    private final @NotNull Table<?> table;
    private final @NotNull TableField<?, ID> idColumn;
    private final @NotNull Class<T> dataType;
    private final @NotNull Executor executor;

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return query(dsl ->
                dsl.selectFrom(table)
                        .where(idColumn.eq(id))
                        .fetchOptionalInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return query(dsl -> dsl.fetchExists(
                dsl.selectOne()
                        .from(table)
                        .where(idColumn.eq(id))
        ));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return query(dsl ->
                dsl.selectFrom(table)
                        .fetchInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T data) {
        return query(dsl -> {
            Record record = dsl.newRecord(table, data);
            return dsl.insertInto(table)
                    .set(record)
                    .onConflict(idColumn)
                    .doUpdate()
                    .set(record)
                    .returning()
                    .fetchOneInto(dataType);
        });
    }

    @Override
    public @NotNull CompletableFuture<?> delete(final @NotNull ID id) {
        return query(dsl ->
                dsl.deleteFrom(table)
                        .where(idColumn.eq(id))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAllById(final @NotNull Collection<ID> ids) {
        return query(dsl -> dsl.selectFrom(table)
                .where(idColumn.in(ids))
                .fetchInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries) {
        if (entries.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return query(dsl -> {
            List<Record> records = entries.stream()
                    .map(entry -> dsl.newRecord(table, entry))
                    .collect(Collectors.toList());
            dsl.batch(
                            dsl.insertInto(table)
                                    .set(records.get(0))
                                    .onConflict(idColumn)
                                    .doUpdate()
                                    .set(records.get(0))
                    ).bind(
                            records.stream()
                                    .map(r -> {
                                        Object[] original = r.intoArray();
                                        Object[] doubled = new Object[original.length * 2];
                                        System.arraycopy(original, 0, doubled, 0, original.length);
                                        System.arraycopy(original, 0, doubled, original.length, original.length);
                                        return doubled;
                                    })
                                    .toArray(Object[][]::new)
                    )
                    .execute();
            return records.stream()
                    .map(r -> r.get(idColumn))
                    .collect(Collectors.toList());
        }).thenCompose(this::findAllById);
    }

    @Override
    public @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids) {
        return query(dsl ->
                dsl.deleteFrom(table)
                        .where(idColumn.in(ids))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return query(dsl -> dsl.selectCount()
                .from(table)
                .fetchOne(0, Long.class));
    }

    /**
     * Executes a general query and returns the result.
     *
     * @param <R>           the type parameter
     * @param queryFunction the query
     * @return the result
     */
    public <R> @NotNull CompletableFuture<R> query(final @NotNull Function<DSLContext, R> queryFunction) {
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(context), executor);
    }

}
