package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.sql.helper.H2IntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2DataSourceIntegrationTest extends SqlDataSourceIntegrationTest {
    private final HikariConfig config = new HikariConfig()

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    void setup() {
        config.username = 'sa'
        config.password = ''
    }

    def 'test initialize h2 disk connection throws on non-existing'() {
        when:
        SqlDataSource.builder()
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
    }

    def 'test memory datasource life cycle'() {
        given:
        def builder = SqlDataSource.builder()
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

    def 'test init script works'() {
        given:
        final context = testHelper.context

        and:
        def dataSource = (newDataSourceBuilder() as H2DataSourceBuilder)
                .initScript('build/resources/integrationTest/schema.sql')

        when:
        dataSource.build()

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
    }

    def 'test that database is initialized with custom schema'() {
        given:
        def builder = new H2DataSourceBuilder(config, 'test', executor)
                .memory()
                .schemaName('custom')

        when:
        def dataSource = builder.build()
        def connection = dataSource.dataSource.connection

        then:
        connection.catalog == 'TEST'
        connection.schema == 'CUSTOM'

        cleanup:
        connection?.close()
        dataSource?.close()
    }

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder()
                .h2()
                .database('h2')
                .disk('./build/resources/integrationTest')
                .allowSimultaneousFileConnections()
                .schemaName('PUBLIC')
                .lowercaseNames(true)
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2IntegrationTestHelper()
    }

}
