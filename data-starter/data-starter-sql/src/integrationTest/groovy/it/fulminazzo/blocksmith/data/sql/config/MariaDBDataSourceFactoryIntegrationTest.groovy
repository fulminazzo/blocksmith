package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.helper.MariaDBIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class MariaDBDataSourceFactoryIntegrationTest extends SqlDataSourceFactoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected SqlDataSourceConfig newConfig() {
        return new SqlDataSourceConfig()
                .withDatabaseType(DatabaseType.MARIADB)
                .withHost(testHelper.serverHost)
                .withPort(testHelper.serverPort)
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new MariaDBIntegrationTestHelper()
    }

}
