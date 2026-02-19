package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.memory.MemoryQueryEngine
import it.fulminazzo.blocksmith.data.memory.MemoryRepository
import org.jetbrains.annotations.NotNull

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CachedRepositoryTest extends RepositoryTest<CachedRepository<User, Long>> {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static final EntityMapper<User, Long> entityMapper = EntityMapper.create(User)
    private static final MemoryRepository<User, Long> cache = new MemoryRepository(
            new MemoryQueryEngine<>(executor),
            entityMapper
    ).ttl(Duration.ofMinutes(1))
    private static final MemoryRepository<User, Long> actual = new MemoryRepository<User, Long>(
            new MemoryQueryEngine<User, Long>(executor),
            entityMapper
    ) {

        @Override
        protected @NotNull
        CompletableFuture<User> saveImpl(final @NotNull User entity) {
            if (entity.id == -1L) entity.id = 3L
            return super.saveImpl(entity)
        }

        @Override
        protected @NotNull
        CompletableFuture<Collection<User>> saveAllImpl(final @NotNull Collection<User> entities) {
            entities.each {
                if (it.id == -1L) it.id = 3L
            }
            return super.saveAllImpl(entities)
        }

    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        executor.shutdown()
    }

    def 'test that findById updates cache repository if not present'() {
        given:
        def entity = Users.SAVED1

        expect:
        !cache.existsById(entity).get()

        when:
        def found = repository.findById(entity.id).get()

        then:
        found.isPresent()

        and:
        found.get() == entity

        and:
        cache.existsById(entity.id).get()
    }

    def 'test that save saves on cache repository the updated version of entity'() {
        given:
        def expected = Users.NEW1
        def entity = new User(-1L, expected.username, expected.age)

        when:
        repository.save(entity).get()

        and:
        def actual = cache.findById(expected.id).get()

        then:
        actual.isPresent()

        when:
        def actualEntity = actual.get()

        then:
        actualEntity == expected
    }

    def 'test that delete deletes from both repositories'() {
        given:
        def entityId = Users.SAVED2.id

        expect:
        cache.existsById(entityId).get()
        actual.existsById(entityId).get()

        when:
        repository.delete(entityId).get()

        then:
        !cache.existsById(entityId).get()
        !actual.existsById(entityId).get()
    }

    def 'test that findAll returns only all the entities of the main repository'() {
        given:
        cache.save(Users.NEW1).get()

        when:
        def actual = repository.findAll().get()

        then:
        actual == [Users.SAVED1, Users.SAVED2]
    }

    def 'test that findAllById queries main repository only for not found entities'() {
        given:
        def expected = [Users.SAVED2, Users.NEW1, Users.SAVED1]

        and:
        cache.save(Users.NEW1).get()

        when:
        def actual = repository.findAllById(
                [*expected.collect { it.id }, 9L]
        ).get()

        then:
        actual == expected
    }

    def 'test that saveAll saves on cache repository the updated version of entities'() {
        given:
        def expected = [Users.NEW1, Users.NEW2]
        def entities = [
                new User(-1L, Users.NEW1.username, Users.NEW1.age),
                Users.NEW2
        ]

        when:
        repository.saveAll(entities).get()

        and:
        def actual = cache.findAllById(expected.collect { it.id }).get()

        then:
        actual == expected
    }

    def 'test that deleteAll deletes from both repositories'() {
        given:
        def entitiesIds = [Users.SAVED2].collect { it.id }

        expect:
        cache.existsById(entitiesIds[0]).get()
        actual.existsById(entitiesIds[0]).get()

        when:
        repository.deleteAll(entitiesIds).get()

        then:
        !cache.existsById(entitiesIds[0]).get()
        !actual.existsById(entitiesIds[0]).get()
    }

    @Override
    CachedRepository<User, Long> initializeRepository() {
        return new CachedRepository<User, Long>(
                entityMapper,
                cache,
                actual
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return cache.existsById(id).get() || actual.existsById(id).get()
    }

    @Override
    void insert(final @NotNull User entity) {
        if (entity == Users.SAVED2) cache.save(entity).get()
        actual.save(entity).get()
    }

    @Override
    void remove(final @NotNull Long id) {
        cache.delete(id).get()
        actual.delete(id).get()
    }

}
