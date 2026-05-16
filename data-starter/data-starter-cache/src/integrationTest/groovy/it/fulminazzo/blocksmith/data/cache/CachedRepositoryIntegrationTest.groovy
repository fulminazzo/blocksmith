package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryIntegrationTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepository
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.redis.RedisRepository
import it.fulminazzo.blocksmith.data.sql.SqlRepository
import org.jetbrains.annotations.NotNull
import spock.lang.Shared

import java.time.Duration

class CachedRepositoryIntegrationTest extends RepositoryIntegrationTest<CachedRepository<User, Long>> {
    private static final CachedIntegrationTestHelper TEST_HELPER = new CachedIntegrationTestHelper()

    @Shared
    private Repository<User, Long> cache

    @Shared
    private Repository<User, Long> base

    void setupSpec() {
        cache = TEST_HELPER.cacheRepository
        base = TEST_HELPER.baseRepository
    }

    void cleanupSpec() {
        TEST_HELPER.close()
    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    @Override
    void setupRepository() {
        super.setupRepository()
        cache.delete(Users.SAVED2.id).join()
    }

    def 'test that wrap allows creation of a cached repository wrapping the original'() {
        given:
        def memoryDataSource = MemoryDataSource.create(TEST_HELPER.executor)

        when:
        def repository = CachedRepository.wrap(base)
                ."$entityMapperMethod"(*entityMapperArguments)
                .cacheRepository(TEST_HELPER.cacheDataSource, TEST_HELPER.cacheSettings)
                .hybrid(
                        memoryDataSource,
                        new MemoryRepositorySettings()
                                .withTtl(Duration.ofSeconds(5L))
                )

        then:
        repository != null

        and:
        MemoryRepository.isInstance(repository.cacheRepository)

        and:
        def baseRepository = repository.repository
        CachedRepository.isInstance(baseRepository)
        RedisRepository.isInstance(baseRepository.cacheRepository)
        SqlRepository.isInstance(baseRepository.repository)

        where:
        entityMapperMethod | entityMapperArguments
        'entityType'       | [User]
        'entityMapper'     | [EntityMapper.create(User)]
    }

    @Override
    CachedRepository<User, Long> initializeRepository() {
        return new CachedRepository<User, Long>(
                cache,
                base,
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return cache.existsById(id).get() || base.existsById(id).get()
    }

    @Override
    void insert(final @NotNull User entity) {
        if (entity == Users.SAVED2) cache.save(entity).get()
        base.save(entity).get()
    }

    @Override
    void remove(final @NotNull Long id) {
        cache.delete(id).get()
        base.delete(id).get()
    }

}
