package it.fulminazzo.blocksmith.data.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Mongo data source for handling connections and create Mongo repositories.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         MongoDataSource datasource = MongoDataSource.builder()
 *                 // if no host has been specified, will default to "0.0.0.0:27017"
 *                 .host("127.0.0.1", 27017)
 *                 .replicaSetName("rs0")
 *                 .credential(MongoCredential.createScramSha256Credential(
 *                         "username",
 *                         "admin",
 *                         "SuperSecurePassword".toCharArray()
 *                 ))
 *                 .applicationName("blocksmith/1.0.0")
 *                 .sslSettings(ssl -> ssl.enabled(true))
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a new repository:
 *         <pre>{@code
 *         MongoDataSource dataSource = ...;
 *         Class<?> dataType = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 dataType,
 *                 "database",
 *                 "data"
 *         );
 *         }</pre>
 *         or, for more control:
 *         <pre>{@code
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(dataType),
 *                 "database",
 *                 "data"
 *         );
 *         }</pre>
 *     </li>
 * </ul>
 */
public final class MongoDataSource implements RepositoryDataSource {
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
     *
     * @param <T>            the type of the entities
     * @param <ID>           the type of the id of the entities
     * @param entityType     the entity Java class
     * @param databaseName   the name of the collection
     * @param collectionName the name of the database
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType,
            final @NotNull String databaseName,
            final @NotNull String collectionName
    ) {
        return newRepository(EntityMapper.create(entityType), databaseName, collectionName);
    }

    /**
     * Creates a new repository.
     *
     * @param <T>            the type of the entities
     * @param <ID>           the type of the id of the entities
     * @param entityMapper   the entities mapper
     * @param databaseName   the name of the collection
     * @param collectionName the name of the database
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper,
            final @NotNull String databaseName,
            final @NotNull String collectionName
    ) {
        MongoQueryEngine<T, ID> engine = new MongoQueryEngine<>(
                client.getDatabase(databaseName).getCollection(collectionName, entityMapper.getType())
        );
        return new MongoRepository<>(engine, entityMapper);
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull MongoDataSourceBuilder builder() {
        return new MongoDataSourceBuilder();
    }

}
