package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.H2RemoteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2RemoteDataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder()
                .h2()
                .database('h2_remote')
                .server(testHelper.serverHost, testHelper.serverPort, './build/resources/integrationTest')
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2RemoteIntegrationTestHelper()
    }

}
