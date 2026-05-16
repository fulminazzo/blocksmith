package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.helper.PostgresSQLIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class PostgresSQLDataSourceFactoryIntegrationTest extends SqlDataSourceFactoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected SqlDataSourceConfig newConfig() {
        return new SqlDataSourceConfig()
                .setDatabaseType(DatabaseType.POSTGRESQL)
                .setHost(testHelper.serverHost)
                .setPort(testHelper.serverPort)
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new PostgresSQLIntegrationTestHelper()
    }

}
