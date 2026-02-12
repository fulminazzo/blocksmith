package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

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
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 * @param <TB> the type of the table
 */
@SuppressWarnings({"resource"})
public class SqlRepository<T, ID, TB extends Table<?>> extends AbstractRepository<T, ID> {
    private final @NotNull DSLContext context;
    protected final @NotNull TB table;
    protected final @NotNull Field<ID> idColumn;
    private final @NotNull Executor executor;

    protected SqlRepository(final @NotNull DSLContext context,
                            final @NotNull TB table,
                            final @NotNull Field<ID> idColumn,
                            final @NotNull EntityMapper<T, ID> entityMapper,
                            final @NotNull Executor executor) {
        super(entityMapper);
        this.context = context;
        this.table = table;
        this.idColumn = idColumn;
        this.executor = executor;
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return query(dsl ->
                dsl.selectFrom(table())
                        .where(idColumn.eq(id))
                        .fetchOptionalInto(entityMapper.getType())
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return query(dsl -> dsl.fetchExists(
                dsl.selectOne()
                        .from(table())
                        .where(idColumn.eq(id))
        ));
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        return query(dsl -> {
            Record record = dsl.newRecord(table(), entity);
            return dsl.insertInto(table())
                    .set(record)
                    .onConflict(idColumn)
                    .doUpdate()
                    .set(record)
                    .returning()
                    .fetchOneInto(entityMapper.getType());
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return query(dsl ->
                dsl.deleteFrom(table())
                        .where(idColumn.eq(id))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return query(dsl ->
                dsl.selectFrom(table())
                        .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return query(dsl -> dsl.selectFrom(table())
                .where(idColumn.in(ids))
                .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return query(dsl -> {
            List<Record> records = entities.stream()
                    .map(entry -> dsl.newRecord(table(), entry))
                    .collect(Collectors.toList());
            dsl.batch(
                            dsl.insertInto(table())
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
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        if (ids.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        return query(dsl ->
                dsl.deleteFrom(table())
                        .where(idColumn.in(ids))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return query(dsl -> dsl.selectCount()
                .from(table())
                .fetchOne(0, Long.class));
    }

    /**
     * Executes a general query and returns the result.
     *
     * @param <R>           the type of the result
     * @param queryFunction the query
     * @return the result
     */
    protected <R> @NotNull CompletableFuture<R> query(final @NotNull Function<DSLContext, R> queryFunction) {
        return CompletableFuture.supplyAsync(() -> queryFunction.apply(context), executor);
    }

    private @NotNull Table<?> table() {
        return table;
    }

}
