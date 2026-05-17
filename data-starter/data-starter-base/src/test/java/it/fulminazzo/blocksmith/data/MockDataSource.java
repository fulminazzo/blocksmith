package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Mock {@link RepositoryDataSource} for testing purposes.
 *
 * @see MockRepository
 */
public final class MockDataSource implements CacheRepositoryDataSource<MockRepositorySettings> {

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T, I> CacheRepository<T, I> newRepository(
            final @NotNull EntityMapper<T, I> entityMapper,
            final @NonNull MockRepositorySettings settings
    ) {
        return (CacheRepository<T, I>) new MockRepository();
    }

    @Override
    public void close() {
        // no operation needed
    }

}
