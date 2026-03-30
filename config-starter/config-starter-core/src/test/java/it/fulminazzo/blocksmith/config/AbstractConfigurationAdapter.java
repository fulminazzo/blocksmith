package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.File;

abstract class AbstractConfigurationAdapter implements BaseConfigurationAdapter {

    @Override
    public @NonNull <T> T load(final @NotNull File file,
                               final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull File file,
                          final @NonNull T configuration) {
        throw new UnsupportedOperationException();
    }

}
