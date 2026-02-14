package it.fulminazzo.blocksmith.data.mongodb

import com.mongodb.ServerAddress
import spock.lang.Specification

class MongoDataSourceBuilderTest extends Specification {

    def 'test that builder does not set default host if hosts given'() {
        when:
        def dataSource = new MongoDataSourceBuilder()
                .host('0.0.0.0', 27018)
                .host('192.168.1.1', 27019)
                .build()
        def client = dataSource.client

        and:
        def servers = client.clusterDescription.serverDescriptions

        then:
        servers.size() == 2

        when:
        def host = servers[0].address

        then:
        host.host == '0.0.0.0'
        host.port == 27018

        when:
        host = servers[1].address

        then:
        host.host == '192.168.1.1'
        host.port == 27019

        cleanup:
        client?.close()
    }

    def 'test that builder sets default host if none given'() {
        when:
        def dataSource = new MongoDataSourceBuilder().build()
        def client = dataSource.client

        and:
        def servers = client.clusterDescription.serverDescriptions

        then:
        servers.size() == 1

        when:
        def host = servers[0].address

        then:
        host.host == ServerAddress.defaultHost()
        host.port == ServerAddress.defaultPort()

        cleanup:
        client?.close()
    }

}
