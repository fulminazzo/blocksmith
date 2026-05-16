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
                .withUsername('root')
                .withPassword('test')
                .withConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .withType(SqlDataSourceConfig.ConnectionModeType.MEMORY)
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
                .withDatabaseType(DatabaseType.SQLITE)
                .withDatabase('sqlite')
                .withParameters([
                        'DB_CLOSE_ON_EXIT' : true
                ])
                .withConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .withType(SqlDataSourceConfig.ConnectionModeType.DISK)
                                .withDirectoryPath('build/resources/integrationTest')
                )
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new SQLiteIntegrationTestHelper()
    }

}
