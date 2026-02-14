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
    @Nullable ConfigurationFormat format;
    @Nullable BaseConfigurationAdapter delegate;

    @Override
    public <T> @NotNull T load(final @NotNull File parentDirectory,
                               final @NotNull String fileName,
                               final @NotNull Class<T> type) throws IOException {
        return load(getFormat().getFile(parentDirectory, fileName), type);
    }

    @Override
    public <T> void store(final @NotNull File parentDirectory,
                          final @NotNull String fileName,
                          final @NotNull T configuration) throws IOException {
        store(getFormat().getFile(parentDirectory, fileName), configuration);
    }

    @Override
    public <T> @NotNull T load(final @NotNull File file,
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
        this.format = format;
        this.delegate = format.newAdapter(logger);
        return this;
    }

    private @NotNull ConfigurationFormat getFormat() {
        if (format == null)
            throw new IllegalStateException("format has not been initialized yet");
        return format;
    }

    private @NotNull BaseConfigurationAdapter getDelegate() {
        if (delegate == null)
            throw new IllegalStateException("delegate has not been initialized yet");
        return delegate;
    }

}
