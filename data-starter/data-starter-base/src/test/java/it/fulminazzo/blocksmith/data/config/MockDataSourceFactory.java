package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.data.MockDataSource;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import org.jetbrains.annotations.NotNull;

/**
 * Mock {@link DataSourceFactory} for testing purposes.
 *
 * @see MockDataSourceConfig
 */
public final class MockDataSourceFactory implements DataSourceFactory {

    @Override
    public @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config) {
        return new MockDataSource();
    }

}
