package it.fulminazzo.blocksmith.data.mongodb.config

import it.fulminazzo.blocksmith.data.mongodb.MongoIntegrationTest
import spock.lang.Specification

class MongoDataSourceIntegrationTest extends Specification implements MongoIntegrationTest {

    def 'test build with #config'() {
        when:
        def dataSource = new MongoDataSourceFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        config << [
                new MongoDataSourceConfig()
                        .withHost(serverHost)
                        .withPort(serverPort),
                new MongoDataSourceConfig()
                        .withHost(serverHost)
                        .withPort(serverPort)
                        .withSrvMaxHosts(1)
                        .withSrvServiceName('test'),
                new MongoDataSourceConfig()
                        .withHost(serverHost)
                        .withPort(serverPort)
                        .withReplicaSetName('rs0'),
                new MongoDataSourceConfig()
                        .withHost(serverHost)
                        .withPort(serverPort)
                        .withApplicationName('test'),
                new MongoDataSourceConfig()
                        .withHost(serverHost)
                        .withPort(serverPort)
                        .withCredentials(
                                new MongoDataSourceConfig.MongoCredentialConfig()
                                        .withUsername('root')
                                        .withPassword('test')
                                        .withMechanism('SCRAM_SHA_256')
                        )
        ]
    }

}
