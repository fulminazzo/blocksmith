package it.fulminazzo.blocksmith.data.mongodb.config

import spock.lang.Specification

class MongoDataSourceFactoryTest extends Specification {
    private static final String HOST = 'localhost'
    private static final int PORT = 27017

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
                        .host(HOST)
                        .port(PORT)
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(HOST)
                        .port(PORT)
                        .srvMaxHosts(1)
                        .srvServiceName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(HOST)
                        .port(PORT)
                        .replicaSetName('replica')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(HOST)
                        .port(PORT)
                        .applicationName('test')
                        .build(),
                MongoDataSourceConfig.builder()
                        .host(HOST)
                        .port(PORT)
                        .credentials(MongoDataSourceConfig.MongoCredentialConfig.builder()
                                .username('test')
                                .password('password')
                                .build())
                        .build()
        ]
    }

}
