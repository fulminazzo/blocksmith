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
                        .setHost(serverHost)
                        .setPort(serverPort),
                new MongoDataSourceConfig()
                        .setHost(serverHost)
                        .setPort(serverPort)
                        .setSrvMaxHosts(1)
                        .setSrvServiceName('test'),
                new MongoDataSourceConfig()
                        .setHost(serverHost)
                        .setPort(serverPort)
                        .setReplicaSetName('rs0'),
                new MongoDataSourceConfig()
                        .setHost(serverHost)
                        .setPort(serverPort)
                        .setApplicationName('test'),
                new MongoDataSourceConfig()
                        .setHost(serverHost)
                        .setPort(serverPort)
                        .setCredentials(
                                new MongoDataSourceConfig.MongoCredentialConfig()
                                        .setUsername('root')
                                        .setPassword('test')
                                        .setMechanism('SCRAM_SHA_256')
                        )
        ]
    }

}
