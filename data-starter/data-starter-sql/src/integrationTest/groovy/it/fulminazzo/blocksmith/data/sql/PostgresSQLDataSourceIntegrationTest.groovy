package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.PostgresSQLIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class PostgresSQLDataSourceIntegrationTest extends RemoteSqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new PostgresSQLIntegrationTestHelper()
    }

}
