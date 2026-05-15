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
        testHelper.context.dropTableIfExists('logins').execute()
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

    def 'test #methodName of #argument correctly updates database'() {
        given:
        final context = testHelper.context

        and:
        def dataSource = newDataSourceBuilder().build()

        when:
        dataSource."$methodName"(argument)

        then:
        noExceptionThrown()

        when:
        def results = context.selectFrom('logins').fetchMany()

        then:
        results.size() == 1

        and:
        def result = results[0]
        result.getValue(0, 'name') == 'Alex'
        result.getValue(0, 'count') == 3

        cleanup:
        context.dropTableIfExists('logins').execute()

        where:
        methodName                  | argument
        'executeScriptFromFile'     | 'build/resources/integrationTest/schema.sql'
        'executeScriptFromFile'     | new File('build/resources/integrationTest/schema.sql')
        'executeScriptFromResource' | '/schema.sql'
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
