package it.fulminazzo.blocksmith.data.mongodb

import it.fulminazzo.blocksmith.data.DataSourceIntegrationTest
import it.fulminazzo.blocksmith.data.RepositoryDataSource
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.bson.UuidRepresentation

import java.util.concurrent.TimeUnit

class MongoDataSourceIntegrationTest extends DataSourceIntegrationTest<MongoRepositorySettings> implements MongoIntegrationTest {

    def 'test datasource life cycle with explicit EntityMapper'() {
        given:
        def builder = newDataSourceBuilder()

        when:
        def dataSource = builder.build()

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(
                User,
                settings.withEntityMapper(EntityMapper.create(User, 'username'))
        )

        then:
        repository != null

        when:
        def user = repository.findById('Alex').join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    @Override
    protected RepositoryDataSourceBuilder<RepositoryDataSource<MongoRepositorySettings>> newDataSourceBuilder() {
        return MongoDataSource.builder()
                .host(serverHost, serverPort)
                .applicationName('mongo-datasource-test/1.0.0')
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .loggerSettings { it.maxDocumentLength(1000) }
                .socketSettings { it.connectTimeout(10, TimeUnit.SECONDS) }
                .connectionPoolSettings {
                    it
                            .maxSize(100)
                            .maxWaitTime(2, TimeUnit.MINUTES)
                            .maintenanceFrequency(1, TimeUnit.MINUTES)
                            .maxConnecting(2)
                }
                .serverSettings {
                    it
                            .heartbeatFrequency(10, TimeUnit.SECONDS)
                            .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS)
                }
                .sslSettings { it.enabled(false) }
    }

    @Override
    protected MongoRepositorySettings getSettings() {
        return new MongoRepositorySettings()
                .withDatabaseName('database')
                .withCollectionName('users')
    }

}
