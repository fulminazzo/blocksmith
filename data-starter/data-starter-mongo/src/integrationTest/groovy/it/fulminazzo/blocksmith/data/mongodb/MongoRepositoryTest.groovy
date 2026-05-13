package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.annotations.NotNull
import reactor.core.publisher.Mono
import spock.lang.Shared

class MongoRepositoryTest extends RepositoryTest<MongoRepository<User, Long>> implements MongoIntegrationTest {
    private static final String ID_FIELD_NAME = '_id'

    @Shared
    private MongoClient client

    @Shared
    private MongoCollection<User> collection

    void setupSpec() {
        def pojoCodec = CodecRegistries.fromRegistries(MongoClientSettings.defaultCodecRegistry,
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        client = MongoClients.create(connectionString)
        def database = client.getDatabase('test').withCodecRegistry(pojoCodec)
        collection = database.getCollection('users', User)
    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        client?.close()
    }

    @Override
    MongoRepository<User, Long> initializeRepository() {
        return new MongoRepository<User, Long>(
                new MongoQueryEngine<>(collection),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return Mono.from(collection.find(Filters.eq(ID_FIELD_NAME, id))).block() != null
    }

    @Override
    void insert(final @NotNull User entity) {
        Mono.from(collection.insertOne(entity)).block()
    }

    @Override
    void remove(final @NotNull Long id) {
        Mono.from(collection.deleteOne(Filters.eq(ID_FIELD_NAME, id))).block()
    }

}
