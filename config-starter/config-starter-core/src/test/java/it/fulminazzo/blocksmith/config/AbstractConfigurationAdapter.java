package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;

abstract class AbstractConfigurationAdapter implements BaseConfigurationAdapter {

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull File file, final @NotNull T configuration) {
        throw new UnsupportedOperationException();
    }

}
