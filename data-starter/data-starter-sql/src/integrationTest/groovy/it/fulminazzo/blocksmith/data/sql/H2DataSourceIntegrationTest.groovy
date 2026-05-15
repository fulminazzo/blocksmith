package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.pool.HikariPool
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.sql.helper.H2IntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2DataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    def 'test initialize h2 disk connection throws on non-existing'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('h2')
                .username('sa')
                .password('')
                .h2()
                .disk('./build/resources/integrationTest/h2_data_source_invalid/')
                .allowSimultaneousFileConnections()
                .preventConnectionOnNonExistingFile()
                .build()

        then:
        thrown(HikariPool.PoolInitializationException)

        cleanup:
        source?.close()
    }

    def 'test memory datasource life cycle'() {
        given:
        def builder = SqlDataSource.builder()
                .executor(executor)
                .database('h2')
                .username('sa')
                .password('')
                .h2()
                .memory()
                .preventMemoryLoss()

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
                .h2()
                .database('h2')
                .disk('./build/resources/integrationTest')
                .allowSimultaneousFileConnections()
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2IntegrationTestHelper()
    }

}
