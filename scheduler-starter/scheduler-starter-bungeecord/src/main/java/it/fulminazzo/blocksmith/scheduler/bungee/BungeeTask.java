package it.fulminazzo.blocksmith.scheduler.bungee;

import it.fulminazzo.blocksmith.scheduler.Task;
import lombok.Setter;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BungeeTask implements Task {
    @Setter
    private @Nullable ScheduledTask internal;
    private boolean cancelled;

    @Override
    public void cancel() {
        getInternal().cancel();
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public @NotNull Object getOwner() {
        return getInternal().getOwner();
    }

    @Override
    public boolean isAsync() {
        return true; // always asynchronous in Bungeecord
    }

    private @NotNull ScheduledTask getInternal() {
        if (internal == null)
            throw new IllegalStateException("internal task has not been set yet");
        return internal;
    }

}
