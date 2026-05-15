package it.fulminazzo.blocksmith.data.mongodb

import it.fulminazzo.blocksmith.data.DataSourceIntegrationTest
import it.fulminazzo.blocksmith.data.RepositoryDataSource
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder

class MongoDataSourceIntegrationTest extends DataSourceIntegrationTest<MongoRepositorySettings> implements MongoIntegrationTest {

    @Override
    protected RepositoryDataSourceBuilder<RepositoryDataSource<MongoRepositorySettings>> newDataSourceBuilder() {
        return MongoDataSource.builder()
                .host(serverHost, serverPort)
                .applicationName('mongo-datasource-test/1.0.0')
    }

    @Override
    protected MongoRepositorySettings getSettings() {
        return new MongoRepositorySettings()
                .withDatabaseName('database')
                .withCollectionName('users')
    }

}
