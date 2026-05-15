package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.MySQLIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class MySQLDataSourceIntegrationTest extends RemoteSqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new MySQLIntegrationTestHelper()
    }

}
