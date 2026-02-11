package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.annotations.NotNull
import reactor.core.publisher.Mono

class MongoRepositoryTest extends RepositoryTest<MongoRepository<User, Long>> {
    private static final int port = 47017
    private static final String idFieldName = 'id'

    private TransitionWalker.ReachedState<RunningMongodProcess> server
    private MongoClient client
    private MongoCollection<User> collection

    void setup() {
        server = Mongod.instance().start(Version.V7_0_0)

        def pojoCodec = CodecRegistries.fromRegistries(MongoClientSettings.defaultCodecRegistry,
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        client = MongoClients.create("mongodb://localhost:$port")
        def database = client.getDatabase('test').withCodecRegistry(pojoCodec)
        collection = database.getCollection('users', User)

        setupRepository()
    }

    def 'test that query works'() {
        when:
        def documents = repository.query(c -> c.countDocuments()).get()

        then:
        documents == 2
    }

    void cleanup() {
        if (client != null) client.close()
        if (server != null) server.close()
    }

    @Override
    MongoRepository<User, Long> initializeRepository() {
        return new MongoRepository<User, Long>(
                collection,
                idFieldName,
                User::getId
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return Mono.from(collection.find(Filters.eq(idFieldName, id))).block() != null
    }

    @Override
    void insert(final @NotNull User data) {
        Mono.from(collection.insertOne(data)).block()
    }

}
