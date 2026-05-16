package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepository
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.redis.RedisRepository
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.sql.SqlRepository
import org.jetbrains.annotations.NotNull
import org.spockframework.util.Pair
import spock.lang.Shared

import java.time.Duration

class CachedRepositoryTest extends RepositoryTest<CachedRepository<User, Long>> {
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
                .cacheRepository(
                        TEST_HELPER.cacheDataSource,
                        new RedisRepositorySettings()
                                .withDatabaseName('test')
                                .withCollectionName('users')
                                .withTtl(Duration.ofMinutes(1L))
                )
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

    def 'test that fetch from hybrid repository is quicker for higher cache layer'() {
        given:
        final entity = new User(10L, 'Smith', 24)
        def entityId = entity.id
        final cacheTtl = 4L

        and:
        def memoryDataSource = MemoryDataSource.create(TEST_HELPER.executor)

        and:
        def repository = CachedRepository.wrap(base)
                .entityType(User)
                .cacheRepository(
                        TEST_HELPER.cacheDataSource,
                        new RedisRepositorySettings()
                                .withDatabaseName('test')
                                .withCollectionName('users')
                                .withTtl(Duration.ofSeconds(cacheTtl))
                )
                .hybrid(
                        memoryDataSource,
                        new MemoryRepositorySettings()
                                .withTtl(Duration.ofSeconds((long) (cacheTtl / 2)))
                ) as Repository<User, Long>

        when:
        repository.save(entity).join()

        then:
        noExceptionThrown()

        when: 'Fetch from first layer: in-memory repository'
        def memory = timed { repository.findById(entityId).join() }

        then:
        def result1 = memory.first()
        result1.present
        result1.get() == entity

        when: 'Fetch from second layer: Redis repository'
        sleep((long) (cacheTtl / 2 * 1_000))
        def redis = timed { repository.findById(entityId).join() }

        then:
        def result2 = redis.first()
        result2.present
        result2.get() == entity

        and: 'in-memory lookup should be faster than Redis'
        memory.second() <= redis.second()

        when: 'Fetch from third layer: SQL repository'
        sleep((long) (cacheTtl + cacheTtl / 2) * 1_000)
        def sql = timed { repository.findById(entityId).join() }

        then:
        def result3 = sql.first()
        result3.present
        result3.get() == entity

        and: 'Redis lookup should be faster than SQL'
        redis.second() <= sql.second()

        cleanup:
        base.delete(entityId).join()
        cache.delete(entityId).join()
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

    private static <E> Pair<E, Long> timed(final Closure<E> function) {
        def start = now
        def e = function()
        def end = now
        return Pair.of(e, end - start)
    }

    private static long getNow() {
        return System.currentTimeMillis()
    }

}
