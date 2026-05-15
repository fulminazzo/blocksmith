package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.SQLiteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class SQLiteDataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder().database('sqlite')
                .sqlite()
                .disk('./build/resources/integrationTest')
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new SQLiteIntegrationTestHelper()
    }

}
