package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.data.RepositoryIntegrationTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap
import org.jetbrains.annotations.NotNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MemoryRepositoryIntegrationTest extends RepositoryIntegrationTest<MemoryRepository<User, Long>> {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor()
    private static final MemoryQueryEngine<User, Long> ENGINE = new MemoryQueryEngine<>(ExpiringMap.lazy(), EXECUTOR)

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        EXECUTOR?.shutdown()
    }

    @Override
    MemoryRepository<User, Long> initializeRepository() {
        return new MemoryRepository<>(
                ENGINE,
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return ENGINE.internalMap.containsKey(id)
    }

    @Override
    void insert(final @NotNull User entity) {
        ENGINE.internalMap.put(entity.id, entity, 3600_000)
    }

    @Override
    void remove(final @NotNull Long id) {
        ENGINE.internalMap.remove(id)
    }

}
