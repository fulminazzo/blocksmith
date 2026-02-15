package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public final class MemoryRepositorySettings extends RepositorySettings {
    @Getter
    private long expiryInMillis;

    public @NotNull MemoryRepositorySettings withExpiryInMillis(final long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
        return this;
    }

}
