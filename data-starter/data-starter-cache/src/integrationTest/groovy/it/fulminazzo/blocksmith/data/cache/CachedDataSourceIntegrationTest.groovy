package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.*
import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import spock.lang.Specification

class CachedDataSourceIntegrationTest extends Specification {
    private static final CachedIntegrationTestHelper TEST_HELPER = new CachedIntegrationTestHelper()

    void cleanupSpec() {
        TEST_HELPER.close()
    }

    def 'test datasource life cycle'() {
        when:
        def dataSource = CachedDataSource.create(
                TEST_HELPER.cacheDataSource,
                TEST_HELPER.baseDataSource
        )

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(
                User,
                CachedRepositorySettings.combine(
                        TEST_HELPER.cacheSettings,
                        TEST_HELPER.baseSettings
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
    }

}
