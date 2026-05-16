package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.helper.SQLiteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class SQLiteDataSourceFactoryIntegrationTest extends SqlDataSourceFactoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    def 'test build in-memory from config'() {
        given:
        def config = newConfig()
                .setUsername('root')
                .setPassword('test')
                .setConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .setType(SqlDataSourceConfig.ConnectionModeType.MEMORY)
                )

        when:
        def dataSource = new SqlDataSourceFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()
    }

    @Override
    protected SqlDataSourceConfig newConfig() {
        return new SqlDataSourceConfig()
                .setDatabaseType(DatabaseType.SQLITE)
                .setDatabase('sqlite')
                .setParameters([
                        'DB_CLOSE_ON_EXIT' : true
                ])
                .setConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .setType(SqlDataSourceConfig.ConnectionModeType.DISK)
                                .setDirectoryPath('build/resources/integrationTest')
                )
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new SQLiteIntegrationTestHelper()
    }

}
