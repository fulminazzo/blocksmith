package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.memory.MemoryQueryEngine
import it.fulminazzo.blocksmith.data.memory.MemoryRepository
import org.jetbrains.annotations.NotNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CachedRepositoryTest extends RepositoryTest<CachedRepository<
        User, Long,
        MemoryRepository<User, Long>,
        MemoryRepository<User, Long>>
        > {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static final EntityMapper<User, Long> entityMapper = EntityMapper.create(User)
    private static final MemoryRepository<User, Long> first = new MemoryRepository(
            new MemoryQueryEngine<>(executor),
            entityMapper
    )
    private static final MemoryRepository<User, Long> second = new MemoryRepository(
            new MemoryQueryEngine<>(executor),
            entityMapper
    )

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        executor.shutdown()
    }

    @Override
    CachedRepository<User, Long, MemoryRepository<User, Long>, MemoryRepository<User, Long>> initializeRepository() {
        return new CachedRepository<User, Long, MemoryRepository<User, Long>, MemoryRepository<User, Long>>(
                entityMapper,
                first,
                second
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return first.existsById(id).get() || second.existsById(id).get()
    }

    @Override
    void insert(final @NotNull User entity) {
        if (entity == Users.SAVED2) first.save(entity).get()
        second.save(entity).get()
    }

    @Override
    void remove(final @NotNull Long id) {
        first.delete(id).get()
        second.delete(id).get()
    }

}
