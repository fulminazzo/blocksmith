package it.fulminazzo.blocksmith.data.mongodb.config

import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import it.fulminazzo.blocksmith.data.mongodb.TestUtils
import spock.lang.Specification

class MongoDataSourceFactoryTest extends Specification {
    private static final int serverPort = 47020

    private static TransitionWalker.ReachedState<RunningMongodProcess> server

    void setupSpec() {
        server = TestUtils.startServer(serverPort)
    }

    void cleanupSpec() {
        server?.close()
    }

    def 'test build with #config'() {
        when:
        def dataSource = new MongoDataSourceFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        config << [
                MongoDataSourceConfig.builder()
                        .host('0.0.0.0')
                        .port(serverPort)
                        .build(),
                MongoDataSourceConfig.builder()
                        .host('0.0.0.0')
                        .port(serverPort)
                        .srvMaxHosts(1)
                        .srvServiceName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host('0.0.0.0')
                        .port(serverPort)
                        .replicaSetName('replica')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host('0.0.0.0')
                        .port(serverPort)
                        .applicationName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host('0.0.0.0')
                        .port(serverPort)
                        .credentials(MongoDataSourceConfig.MongoCredentialConfig.builder()
                                .username('test')
                                .password('password')
                                .build())
                        .build()
        ]
    }

}
