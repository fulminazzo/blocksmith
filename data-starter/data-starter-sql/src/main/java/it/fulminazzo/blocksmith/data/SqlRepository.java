package it.fulminazzo.blocksmith.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * A basic implementation of {@link Repository} for SQL databases.
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id of the data (should be unique)
 */
@SuppressWarnings({"resource", "unchecked"})
@RequiredArgsConstructor
public class SqlRepository<T, ID> implements Repository<T, ID> {
    private final @NotNull DSLContext context;
    private final @NotNull String tableName;
    private final @NotNull String idColumn;
    private final @NotNull Class<T> dataType;

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return query(dsl ->
                dsl.selectFrom(tableName)
                        .where(field(idColumn).eq(id))
                        .fetchOptionalInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return query(dsl -> dsl.fetchExists(
                dsl.selectOne()
                        .from(table(tableName))
                        .where(field(idColumn).eq(id))
        ));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return query(dsl ->
                dsl.selectFrom(tableName)
                        .fetchInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T data) {
        return query(dsl -> {
            Record record = dsl.newRecord(table(tableName), data);
            dsl.insertInto(table(tableName))
                    .set(record)
                    .onDuplicateKeyUpdate()
                    .set(record)
                    .returning()
                    .fetchOneInto(dataType);
            ID id = (ID) record.get(idColumn);
            return dsl.selectFrom(table(tableName))
                    .where(field(idColumn).eq(id))
                    .fetchOneInto(dataType);
        });
    }

    @Override
    public @NotNull CompletableFuture<?> delete(final @NotNull ID id) {
        return query(dsl ->
                dsl.deleteFrom(table(tableName))
                        .where(field(idColumn).eq(id))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findById(final @NotNull Collection<ID> ids) {
        return query(dsl -> dsl.selectFrom(table(tableName))
                .where(field(idColumn).in(ids))
                .fetchInto(dataType)
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> saveAll(final @NotNull Collection<T> entries) {
        if (entries.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return query(dsl -> {
            List<Record> records = entries.stream()
                    .map(entry -> dsl.newRecord(table(tableName), entry))
                    .collect(Collectors.toList());
            dsl.batch(dsl.insertInto(table(tableName))
                            .set(records.get(0))
                            .onDuplicateKeyUpdate()
                            .set(records.get(0)))
                    .bind(records.stream().map(Record::intoArray).toArray(Object[][]::new))
                    .execute();
            return records.stream()
                    .map(r -> r.get(idColumn))
                    .map(i -> (ID) i)
                    .collect(Collectors.toList());
        }).thenCompose(this::findById);
    }

    @Override
    public @NotNull CompletableFuture<?> deleteAll(final @NotNull Collection<ID> ids) {
        return query(dsl ->
                dsl.deleteFrom(table(tableName))
                        .where(field(idColumn).in(ids))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return query(dsl -> dsl.selectCount()
                .from(table(tableName))
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
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(context));
    }

}
