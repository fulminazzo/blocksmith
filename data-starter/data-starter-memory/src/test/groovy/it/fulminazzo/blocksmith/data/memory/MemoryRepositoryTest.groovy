package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap
import org.jetbrains.annotations.NotNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MemoryRepositoryTest extends RepositoryTest<MemoryRepository<User, Long>> {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor()
    private static final MemoryQueryEngine<User, Long> engine = new MemoryQueryEngine<>(ExpiringMap.lazy(), executor)

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        executor?.shutdown()
    }

    @Override
    MemoryRepository<User, Long> initializeRepository() {
        return new MemoryRepository<>(
                engine,
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return engine.internalMap.containsKey(id)
    }

    @Override
    void insert(final @NotNull User entity) {
        engine.internalMap.put(entity.id, entity)
    }

    @Override
    void remove(final @NotNull Long id) {
        engine.internalMap.remove(id)
    }

}
