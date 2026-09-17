package it.fulminazzo.blocksmith.application;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;

/**
 * Main entry point of a Blocksmith application.
 * TODO: proper documentation with examples
 *
 * @see Application
 */
public abstract class BlocksmithApplication implements Application {
    @Setter
    private @Nullable Application delegate;

    private @NotNull Application getDelegate() {
        if (delegate == null) throw new IllegalStateException("Delegate not set");
        return delegate;
    }

    @Override
    public @NotNull String getName() {
        return getDelegate().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return getDelegate().getVersion();
    }

    @Override
    public @NotNull File getFolder() {
        return getDelegate().getFolder();
    }

    @Override
    public @NotNull Logger getLogger() {
        return getDelegate().getLogger();
    }

    @Override
    public @NotNull <H> H getHost() {
        return getDelegate().getHost();
    }

}
