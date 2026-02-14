package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.PlatformPackageResolver
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.PackageOfCommandDistribution
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.distribution.Distribution
import de.flapdoodle.embed.process.distribution.PackageResolver
import de.flapdoodle.os.CommonOS
import de.flapdoodle.os.ImmutablePlatform
import de.flapdoodle.os.linux.LinuxDistribution
import de.flapdoodle.os.linux.UbuntuVersion
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
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
        server = Mongod.builder()
                .net(Start.to(Net).initializedWith(Net.of('localhost', port, de.flapdoodle.net.Net.localhostIsIPv6())))
                .packageOfDistribution(PackageOfCommandDistribution.builder()
                        .commandPackageResolver(command -> {
                            def resolver = new PlatformPackageResolver(command)
                            return (PackageResolver) ((distribution) -> {
                                try {
                                    return resolver.packageFor(distribution)
                                } catch (IllegalArgumentException e) {
                                    if (distribution.platform().operatingSystem() == CommonOS.Linux) {
                                        def fallbackDist = Distribution.of(
                                                distribution.version(),
                                                ImmutablePlatform.copyOf(distribution.platform())
                                                        .withDistribution(LinuxDistribution.Ubuntu)
                                                        .withVersion(UbuntuVersion.Ubuntu_22_04)
                                        )
                                        return resolver.packageFor(fallbackDist)
                                    }
                                    throw e
                                }
                            })
                        })
                        .build()
                )
                .build()
                .start(Version.Main.V7_0)

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
