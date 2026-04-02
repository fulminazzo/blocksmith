package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.annotations.NotNull
import reactor.core.publisher.Mono

class MongoRepositoryTest extends RepositoryTest<MongoRepository<User, Long>> {
    private static final int port = 47017
    private static final String idFieldName = '_id'

    private static TransitionWalker.ReachedState<RunningMongodProcess> server
    private static MongoClient client
    private static MongoCollection<User> collection

    void setupSpec() {
        server = TestUtils.startServer(port)

        def pojoCodec = CodecRegistries.fromRegistries(MongoClientSettings.defaultCodecRegistry,
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        client = MongoClients.create("mongodb://localhost:$port")
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
        server?.close()
    }

    def 'test that server is online'() {
        expect:
        TestUtils.isRunning(server)
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
        return Mono.from(collection.find(Filters.eq(idFieldName, id))).block() != null
    }

    @Override
    void insert(final @NotNull User entity) {
        Mono.from(collection.insertOne(entity)).block()
    }

    @Override
    void remove(final @NotNull Long id) {
        Mono.from(collection.deleteOne(Filters.eq(idFieldName, id))).block()
    }

}
