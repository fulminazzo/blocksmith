package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper
import spock.lang.Shared
import spock.lang.Specification

abstract class SqlDataSourceFactoryIntegrationTest extends Specification {

    @Shared
    protected SqlIntegrationTestHelper testHelper

    void setupSuite() {
        testHelper = newTestHelper()
    }

    void cleanupSuite() {
        testHelper?.close()
    }

    def 'test build from config'() {
        given:
        def config = newConfig()
                .withUsername('root')
                .withPassword('test')
                .withMaximumPoolSize(20)
                .withMinimumIdle(5)
                .withConnectionTimeout(30_000)
                .withIdleTimeout(10 * 60_000)
                .withMaxLifeTime(30 * 60_000)
                .withProperties(['prepStmtCacheSize' : 250])
        config.database = config.database ?: 'test'

        when:
        def dataSource = new SqlDataSourceFactory().build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()
    }

    protected abstract SqlDataSourceConfig newConfig()

    protected abstract SqlIntegrationTestHelper newTestHelper()

}
