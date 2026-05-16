package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.CustomSqlIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class CustomSqlDataSourceIntegrationTest extends RemoteSqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected IDatabaseType getDatabaseType() {
        return testHelper.databaseType
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new CustomSqlIntegrationTestHelper()
    }

}
