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
                .withDatabaseType(DatabaseType.H2)
                .withHost(testHelper.serverHost)
                .withPort(testHelper.serverPort)
                .withDatabase('h2_remote')
                .withSchemaName('PUBLIC')
                .withParameters([
                        'DB_CLOSE_ON_EXIT' : true
                ])
                .withConnectionMode(
                        new SqlDataSourceConfig.ConnectionMode()
                                .withType(SqlDataSourceConfig.ConnectionModeType.SERVER)
                                .withDirectoryPath('build/resources/integrationTest')
                )
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2RemoteIntegrationTestHelper()
    }

}
