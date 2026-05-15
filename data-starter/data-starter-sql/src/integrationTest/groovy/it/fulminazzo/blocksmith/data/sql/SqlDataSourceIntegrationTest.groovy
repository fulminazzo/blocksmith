package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.DataSourceIntegrationTest
import it.fulminazzo.blocksmith.data.RepositoryDataSource
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper
import spock.lang.Shared

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class SqlDataSourceIntegrationTest extends DataSourceIntegrationTest<SqlRepositorySettings> {
    protected final ExecutorService executor = Executors.newSingleThreadExecutor()

    @Shared
    protected SqlIntegrationTestHelper testHelper

    void setupSuite() {
        testHelper = newTestHelper()
    }

    void cleanupSuite() {
        testHelper?.close()
    }

    def 'test that SQLDialect is expected'() {
        given:
        def builder = newDataSourceBuilder()

        expect:
        builder.SQLDialect == testHelper.dialect
    }

    protected abstract <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl()

    @Override
    protected RepositoryDataSourceBuilder<RepositoryDataSource<SqlRepositorySettings>> newDataSourceBuilder() {
        ASqlDataSourceBuilder builder = newDataSourceBuilderImpl()
        try {
            builder.database
        } catch (NullPointerException ignored) {
            builder.database('test')
        }
        return builder
                .username('root')
                .password('test')
                .executor(executor)
    }

    @Override
    protected SqlRepositorySettings getSettings() {
        return new SqlRepositorySettings()
                .withTable(testHelper.table)
                .withIdColumn(testHelper.column)
    }

    protected abstract SqlIntegrationTestHelper newTestHelper()

}
