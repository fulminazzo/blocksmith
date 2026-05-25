package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.sql.helper.SQLiteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class SQLiteDataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    def 'test memory datasource life cycle'() {
        given:
        def builder = SqlDataSource.builder()
                .database('sqlite')
                .username('sa')
                .password('')
                .sqlite()
                .memory()

        when:
        def dataSource = builder.build()
        def context = SqlIntegrationTestHelper.initializeContextAndTable(dataSource.dataSource, builder.SQLDialect)

        then:
        noExceptionThrown()

        when:
        def table = context.meta().getTables('USERS').last
        def repository = dataSource.newRepository(
                User,
                new SqlRepositorySettings()
                        .withTable(table)
                        .withIdColumn(table.field('ID'))
        )

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder()
                .sqlite()
                .database('sqlite')
                .disk('./build/resources/integrationTest')
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new SQLiteIntegrationTestHelper()
    }

}
