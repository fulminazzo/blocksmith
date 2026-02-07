package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of {@link ConfigurationAdapter} that delegates
 * the loading and storing operations to the adapter associated
 * with the currently chosen format.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
final class DelegateConfigurationAdapter implements ConfigurationAdapter {
    final @NotNull Logger logger;
    @Nullable BaseConfigurationAdapter delegate;

    @Override
    public @NotNull <T> T load(final @NotNull File file,
                               final @NotNull Class<T> type) throws IOException {
        return getDelegate().load(file, type);
    }

    @Override
    public <T> void store(final @NotNull File file,
                          final @NotNull T configuration) throws IOException {
        getDelegate().store(file, configuration);
    }

    @Override
    public @NotNull ConfigurationAdapter setFormat(final @NotNull ConfigurationFormat format) {
        delegate = format.newAdapter(logger);
        return this;
    }

    private @NotNull BaseConfigurationAdapter getDelegate() {
        if (delegate == null)
            throw new IllegalStateException("delegate has not been initialized yet");
        return delegate;
    }

}
