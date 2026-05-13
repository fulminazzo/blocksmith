package it.fulminazzo.blocksmith.data.mongodb

import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

class MongoDataSourceIntegrationTest extends Specification implements MongoIntegrationTest {

    def 'test datasource life cycle'() {
        given:
        def dataSource = MongoDataSource.builder()
                .host(serverHost, serverPort)
                .applicationName('mongo-datasource-test/1.0.0')
                .build()

        when:
        def repository = dataSource.newRepository(
                User,
                new MongoRepositorySettings()
                        .withDatabaseName('database')
                        .withCollectionName('users')
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

}
