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
                .withDatabaseType(DatabaseType.POSTGRESQL)
                .withHost(testHelper.serverHost)
                .withPort(testHelper.serverPort)
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new PostgresSQLIntegrationTestHelper()
    }

}
