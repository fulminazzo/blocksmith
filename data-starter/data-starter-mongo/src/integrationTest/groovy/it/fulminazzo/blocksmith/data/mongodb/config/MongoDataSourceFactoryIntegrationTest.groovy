package it.fulminazzo.blocksmith.data.mongodb.config

import it.fulminazzo.blocksmith.data.mongodb.MongoIntegrationTest
import spock.lang.Specification

class MongoDataSourceFactoryIntegrationTest extends Specification implements MongoIntegrationTest {

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
                        .host(serverHost)
                        .port(serverPort)
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(serverHost)
                        .port(serverPort)
                        .srvMaxHosts(1)
                        .srvServiceName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(serverHost)
                        .port(serverPort)
                        .replicaSetName('replica')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(serverHost)
                        .port(serverPort)
                        .applicationName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(serverHost)
                        .port(serverPort)
                        .credentials(MongoDataSourceConfig.MongoCredentialConfig.builder()
                                .username('test')
                                .password('password')
                                .build())
                        .build()
        ]
    }

}
