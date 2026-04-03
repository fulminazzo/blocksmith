package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

abstract class AbstractConfigurationAdapter implements BaseConfigurationAdapter {

    @Override
    public @NonNull <T> T load(final @NotNull String data, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull <T> T load(final @NotNull InputStream stream, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> String serialize(final @NonNull T configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull File file, final @NonNull T configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull OutputStream stream, final @NonNull T configuration) {
        throw new UnsupportedOperationException();
    }

}
