package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import spock.lang.Specification

import java.time.Duration

class HybridCachedDataSourceIntegrationTest extends Specification {
    private static final CachedIntegrationTestHelper TEST_HELPER = new CachedIntegrationTestHelper()

    void cleanupSpec() {
        TEST_HELPER.close()
    }

    def 'test datasource life cycle'() {
        given:
        def memoryDataSource = MemoryDataSource.create()

        when:
        def dataSource = CachedDataSource.hybrid(
                memoryDataSource,
                TEST_HELPER.cacheDataSource,
                TEST_HELPER.baseDataSource
        )

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(
                User,
                CachedRepositorySettings.combine(
                        new MemoryRepositorySettings().withTtl(Duration.ofSeconds(10L)),
                        CachedRepositorySettings.combine(
                                TEST_HELPER.cacheSettings,
                                TEST_HELPER.baseSettings
                        )
                )
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

        cleanup:
        memoryDataSource.close()
    }

}
