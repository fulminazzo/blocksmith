package it.fulminazzo.blocksmith.scheduler.bukkit;

import it.fulminazzo.blocksmith.scheduler.Task;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BukkitTask implements Task {
    @Setter
    private @Nullable org.bukkit.scheduler.BukkitTask internal;

    @Override
    public void cancel() {
        getInternal().cancel();
    }

    @Override
    public boolean isCancelled() {
        return getInternal().isCancelled();
    }

    @Override
    public @NotNull Object getOwner() {
        return getInternal().getOwner();
    }

    @Override
    public boolean isAsync() {
        return !getInternal().isSync();
    }

    private @NotNull org.bukkit.scheduler.BukkitTask getInternal() {
        if (internal == null)
            throw new IllegalStateException("internal task has not been set yet");
        return internal;
    }

}
