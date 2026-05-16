package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Mock {@link RepositoryDataSource} for testing purposes.
 *
 * @see MockRepository
 */
public final class MockDataSource implements RepositoryDataSource<MockRepositorySettings> {

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T, I> Repository<T, I> newRepository(
            final @NotNull EntityMapper<T, I> entityMapper,
            final @NonNull MockRepositorySettings settings
    ) {
        return (Repository<T, I>) new MockRepository();
    }

    @Override
    public void close() {
        // no operation needed
    }

}
