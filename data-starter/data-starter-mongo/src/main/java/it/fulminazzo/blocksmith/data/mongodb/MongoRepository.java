package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import it.fulminazzo.blocksmith.data.AbstractRepository;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * Implementation of {@link Repository} for MongoDB databases.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities (should be unique)
 */
public class MongoRepository<T, ID> extends AbstractRepository<T, ID, MongoQueryEngine<T, ID>> {

    /**
     * Instantiates a new MongoDB repository.
     *
     * @param queryEngine  the query engine
     * @param entityMapper the entity mapper
     */
    protected MongoRepository(final @NonNull MongoQueryEngine<T, ID> queryEngine,
                              final @NotNull EntityMapper<T, ID> entityMapper) {
        super(queryEngine, entityMapper);
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> findById(final @NonNull ID id) {
        return queryEngine.query(collection ->
                collection.find(eq(getIdFieldName(), id))
        ).thenApply(Optional::ofNullable);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsById(final @NonNull ID id) {
        return queryEngine.query(collection ->
                collection.countDocuments(eq(getIdFieldName(), id), new CountOptions().limit(1))
        ).thenApply(c -> c > 0);
    }

    @Override
    public @NotNull CompletableFuture<T> save(final @NonNull T entity) {
        return queryEngine.query(collection ->
                collection.replaceOne(
                        eq(getIdFieldName(), entityMapper.getId(entity)),
                        entity,
                        new ReplaceOptions().upsert(true)
                )
        ).thenApply(r -> entity);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteImpl(final @NonNull ID id) {
        return queryEngine.query(collection ->
                collection.deleteOne(eq(getIdFieldName(), id))
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<T>> findAll() {
        return queryEngine.queryMany(MongoCollection::find);
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> findAllByIdImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.queryMany(query -> query.find(in(getIdFieldName(), ids)));
    }

    @Override
    protected @NotNull CompletableFuture<Collection<T>> saveAllImpl(final @NotNull Collection<T> entities) {
        List<WriteModel<T>> writeModels = entities.stream()
                .map(e -> new ReplaceOneModel<>(
                        eq(getIdFieldName(), entityMapper.getId(e)),
                        e,
                        new ReplaceOptions().upsert(true)
                ))
                .collect(Collectors.toList());
        return queryEngine.query(collection -> collection.bulkWrite(writeModels))
                .thenApply(result -> entities);
    }

    @Override
    protected @NotNull CompletableFuture<?> deleteAllImpl(final @NotNull Collection<ID> ids) {
        return queryEngine.query(collection ->
                collection.deleteMany(in(getIdFieldName(), ids))
        );
    }

    @Override
    public @NotNull CompletableFuture<Long> count() {
        return queryEngine.query(MongoCollection::countDocuments);
    }

    /**
     * Because of Reactive streams internal conversion from "id" to "_id",
     * we have to check if the field name is "id" and update it.
     *
     * @return the id field name
     */
    private @NonNull String getIdFieldName() {
        String idFieldName = entityMapper.getIdFieldName();
        if (idFieldName.equals("id")) idFieldName = "_" + idFieldName;
        return idFieldName;
    }

}
