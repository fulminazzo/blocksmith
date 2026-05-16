package it.fulminazzo.blocksmith.data.cache.config

import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import it.fulminazzo.blocksmith.data.config.DataSourceFactories
import it.fulminazzo.blocksmith.data.redis.config.RedisDataSourceConfig
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.config.SqlDataSourceConfig
import spock.lang.Specification

class CachedDataSourceFactoryIntegrationTest extends Specification {
    private static final CachedIntegrationTestHelper testHelper = new CachedIntegrationTestHelper()

    void cleanupSpec() {
        testHelper?.close()
    }

    def 'test build from config'() {
        given:
        def config = new CachedDataSourceConfig(
                new RedisDataSourceConfig()
                        .setHost(testHelper.cacheServerHost)
                        .setPort(testHelper.cacheServerPort),
                new SqlDataSourceConfig()
                        .setDatabaseType(DatabaseType.POSTGRESQL)
                        .setHost(testHelper.baseServerHost)
                        .setPort(testHelper.baseServerPort)
                        .setUsername('root')
                        .setPassword('test')
                        .setDatabase('test'),
                hybrid
        )

        when:
        def dataSource = DataSourceFactories.factories[config.class].build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        hybrid << [false, true]
    }

}
