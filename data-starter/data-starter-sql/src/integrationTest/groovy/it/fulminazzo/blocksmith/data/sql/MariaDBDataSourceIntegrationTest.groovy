package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.MariaDBIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class MariaDBDataSourceIntegrationTest extends RemoteSqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected DatabaseType getDatabaseType() {
        return DatabaseType.MARIADB
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new MariaDBIntegrationTestHelper()
    }

}
