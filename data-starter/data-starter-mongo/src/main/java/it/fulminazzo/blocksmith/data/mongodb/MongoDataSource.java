package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import it.fulminazzo.blocksmith.data.Repository;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.function.Function;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Mongo data source for handling connections and create Mongo repositories.
 */
public final class MongoDataSource implements Closeable {
    private final @NotNull MongoClient client;

    /**
     * Instantiates a new Mongo data source.
     *
     * @param client the client
     */
    MongoDataSource(final @NotNull MongoClient client) {
        CodecRegistry pojoCodec = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        this.client = (MongoClient) client.withCodecRegistry(pojoCodec);
    }

    /**
     * Creates a new repository.
     * <br>
     * Assumes the data has a <code>id</code> field as id.
     *
     * @param <T>            the type of the data
     * @param <ID>           the type of the id
     * @param dataType       the data type
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param idMapper       the function to get the id from a data object
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> dataType,
            final @NotNull String databaseName,
            final @NotNull String collectionName,
            final @NotNull Function<T, ID> idMapper
    ) {
        return newRepository(
                dataType,
                databaseName,
                collectionName,
                idMapper,
                "id"
        );
    }

    /**
     * Creates a new repository.
     *
     * @param <T>            the type of the data
     * @param <ID>           the type of the id
     * @param dataType       the data type
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param idMapper       the function to get the id from a data object
     * @param idFieldName    the name of the field representing the id
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> dataType,
            final @NotNull String databaseName,
            final @NotNull String collectionName,
            final @NotNull Function<T, ID> idMapper,
            final @NotNull String idFieldName
    ) {
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<T> collection = database.getCollection(collectionName, dataType);
        return new MongoRepository<>(
                collection,
                idFieldName,
                idMapper
        );
    }

    @Override
    public void close() {
        client.close();
    }

}
