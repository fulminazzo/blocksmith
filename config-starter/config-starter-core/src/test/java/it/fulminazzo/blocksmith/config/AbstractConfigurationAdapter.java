package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

abstract class AbstractConfigurationAdapter implements BaseConfigurationAdapter {

    @Override
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> loadComments(final @NotNull InputStream stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> T load(final @NotNull String data, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> T load(final @NotNull InputStream stream, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> @NotNull String serialize(final @NotNull T configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull OutputStream stream, final @NotNull T configuration) {
        throw new UnsupportedOperationException();
    }

}
