package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link Repository} for SQL databases.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 * @param <TB> the type of the table
 */
@SuppressWarnings({"resource"})
public class SqlRepository<T, ID, TB extends Table<?>> extends AbstractRepository<T, ID, SqlQueryEngine<T, ID, TB>> {

    /**
     * Instantiates a new SQL repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected SqlRepository(final @NotNull SqlQueryEngine<T, ID, TB> queryEngine,
                            final @NotNull EntityMapper<T, ID> entityMapper) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull ID id) {
        return queryEngine.query((dsl, table) ->
                dsl.selectFrom(table)
                        .where(queryEngine.idEquals(id))
                        .fetchOptionalInto(entityMapper.getType())
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull ID id) {
        return queryEngine.query((dsl, table) -> dsl.fetchExists(
                dsl.selectOne()
                        .from(table)
                        .where(queryEngine.idEquals(id))
        ));
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NotNull T entity) {
        return queryEngine.query((dsl, table) -> saveSingle(dsl, table, entity));
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull ID id) {
        return queryEngine.query((dsl, table) ->
                dsl.deleteFrom(table)
                        .where(queryEngine.idEquals(id))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.query((dsl, table) ->
                dsl.selectFrom(table)
                        .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query((dsl, table) -> dsl.selectFrom(table)
                .where(queryEngine.idIn(ids))
                .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        return queryEngine.batched((dsl, table) -> {
            List<T> result = new ArrayList<>();
            for (T e : entities)
                result.add(saveSingle(dsl, table, e));
            return result;
        });
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query((dsl, table) ->
                dsl.deleteFrom(table)
                        .where(queryEngine.idIn(ids))
                        .execute()
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query((dsl, table) ->
                dsl.selectCount()
                        .from(table)
                        .fetchOne(0, Long.class)
        );
    }

    private @NotNull T saveSingle(final @NotNull DSLContext dsl,
                                  final @NotNull Table<?> table,
                                  final @NotNull T entity) {
        Record record = dsl.newRecord(table, entity);
        return Objects.requireNonNull(dsl.insertInto(table)
                .set(record)
                .onConflict(queryEngine.getIdColumn())
                .doUpdate()
                .set(record)
                .returning()
                .fetchOneInto(entityMapper.getType()), "The insertInto query did not return the stored entity accordingly");
    }

}
