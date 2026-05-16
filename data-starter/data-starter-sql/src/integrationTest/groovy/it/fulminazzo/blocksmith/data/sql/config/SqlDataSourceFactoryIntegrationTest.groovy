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
                .setUsername('root')
                .setPassword('test')
                .setMaximumPoolSize(20)
                .setMinimumIdle(5)
                .setConnectionTimeout(30_000)
                .setIdleTimeout(10 * 60_000)
                .setMaxLifeTime(30 * 60_000)
                .setProperties(['prepStmtCacheSize' : 250])
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
