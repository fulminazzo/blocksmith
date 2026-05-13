package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Page;
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
 * @param <T> the type of the entities
 * @param <I> the type of the id of the entities (should be unique)
 * @param <E> the type of the table
 * @see SqlRepositorySettings
 * @see SqlQueryEngine
 */
@SuppressWarnings({"resource"})
public class SqlRepository<T, I, E extends Table<?>> extends AbstractRepository<T, I, SqlQueryEngine<T, I, E>> {

    /**
     * Instantiates a new SQL repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected SqlRepository(
            final @NotNull SqlQueryEngine<T, I, E> queryEngine,
            final @NotNull EntityMapper<T, I> entityMapper
    ) {
        super(queryEngine, entityMapper);
    }

    private @NotNull T saveSingle(
            final @NotNull DSLContext dsl,
            final @NotNull Table<?> table,
            final @NotNull T entity
    ) {
        Record insertRecord = dsl.newRecord(table, entity);
        Record updateRecord = dsl.newRecord(table, entity);
        updateRecord.reset(queryEngine.getIdColumn());

        dsl.insertInto(table)
                .set(insertRecord)
                .onDuplicateKeyUpdate()
                .set(updateRecord)
                .execute();

        I id = insertRecord.get(queryEngine.getIdColumn());
        return Objects.requireNonNull(
                dsl.selectFrom(table)
                        .where(queryEngine.idEquals(id))
                        .fetchOneInto(entityMapper.getType()),
                "Could not retrieve entity after upsert"
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

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NotNull I id) {
        return queryEngine.query((dsl, table) ->
                dsl.selectFrom(table)
                        .where(queryEngine.idEquals(id))
                        .fetchOptionalInto(entityMapper.getType())
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NotNull I id) {
        return queryEngine.query((dsl, table) -> dsl.fetchExists(
                dsl.selectOne()
                        .from(table)
                        .where(queryEngine.idEquals(id))
        ));
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.query((dsl, table) ->
                dsl.selectFrom(table)
                        .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<T> saveImpl(final @NotNull T entity) {
        return queryEngine.query((dsl, table) -> saveSingle(dsl, table, entity));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllImpl(final @NotNull Page page) {
        return queryEngine.query((dsl, table) ->
                dsl.selectFrom(table)
                        .offset(page.getNumber() * page.getSize())
                        .limit(page.getSize())
                        .fetchInto(entityMapper.getType())
        );
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<I> ids) {
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
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<I> ids) {
        return queryEngine.query((dsl, table) ->
                dsl.deleteFrom(table)
                        .where(queryEngine.idIn(ids))
                        .execute()
        );
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NotNull I id) {
        return queryEngine.query((dsl, table) ->
                dsl.deleteFrom(table)
                        .where(queryEngine.idEquals(id))
                        .execute()
        );
    }

}
