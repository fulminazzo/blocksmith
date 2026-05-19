package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.helper.H2IntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2DataSourceFactoryIntegrationTest extends SqlDataSourceFactoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    def 'test build in-memory from config'() {
        given:
        def config = newConfig()
                .setUsername('sa')
                .setPassword('')
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
                .setDatabaseType(DatabaseType.H2)
                .setDatabase('h2')
                .setSchemaName('PUBLIC')
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
        return new H2IntegrationTestHelper()
    }

}
