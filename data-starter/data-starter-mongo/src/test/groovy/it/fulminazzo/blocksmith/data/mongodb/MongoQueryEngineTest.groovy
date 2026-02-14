package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import reactor.core.publisher.Mono
import spock.lang.Specification

class MongoQueryEngineTest extends Specification {
    private static final int port = 47017

    private static TransitionWalker.ReachedState<RunningMongodProcess> server
    private static MongoClient client
    private static MongoCollection<User> collection

    private static MongoQueryEngine<?, ?> queryEngine

    void setupSpec() {
        server = TestUtils.startServer(port)

        def pojoCodec = CodecRegistries.fromRegistries(MongoClientSettings.defaultCodecRegistry,
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        client = MongoClients.create("mongodb://localhost:$port")
        def database = client.getDatabase('test').withCodecRegistry(pojoCodec)
        collection = database.getCollection('users', User)

        queryEngine = new MongoQueryEngine<>(collection)
    }

    void cleanupSpec() {
        client?.close()
        server?.close()
    }

    def 'test that queryMany returns all data'() {
        given:
        def users = [Users.SAVED1, Users.SAVED2, Users.NEW1, Users.NEW2]

        and:
        Mono.from(collection.insertMany(users)).block()

        when:
        def found = queryEngine.queryMany(c ->
                c.find()
        ).get()

        then:
        found == users

        cleanup:
        Mono.from(
                collection.deleteMany(Filters.in('_id', users.collect { it.id }))
        ).block()
    }

    def 'test that query works'() {
        given:
        def user = Users.SAVED1

        when:
        queryEngine.query(c -> c.insertOne(user)).get()

        then:
        noExceptionThrown()

        when:
        def found = queryEngine.query(c ->
                c.find(Filters.eq('_id', user.id))
        ).get()

        then:
        found != null
        found == user

        cleanup:
        Mono.from(
                collection.deleteOne(Filters.eq('_id', user.id))
        ).block()
    }

}
