package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.cache.helper.CachedIntegrationTestHelper
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.jetbrains.annotations.NotNull
import spock.lang.Shared

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
