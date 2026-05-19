package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.helper.H2RemoteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2RemoteDataSourceFactoryIntegrationTest extends SqlDataSourceFactoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected SqlDataSourceConfig newConfig() {
        return new SqlDataSourceConfig()
                .setDatabaseType(DatabaseType.H2)
                .setHost(testHelper.serverHost)
                .setPort(testHelper.serverPort)
                .setDatabase('h2_remote')
                .setSchemaName('PUBLIC')
                .setParameters([
                        'DB_CLOSE_ON_EXIT' : true
                ])
                .setConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .setType(SqlDataSourceConfig.ConnectionModeType.SERVER)
                                .setDirectoryPath('build/resources/integrationTest')
                )
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2RemoteIntegrationTestHelper()
    }

}
