package it.fulminazzo.blocksmith.scheduler.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.fulminazzo.blocksmith.scheduler.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
final class FoliaTask implements Task {
    @Getter
    private final boolean async;

    @Setter
    private @Nullable ScheduledTask internal;

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
        return getInternal().getOwningPlugin();
    }

    private @NotNull ScheduledTask getInternal() {
        if (internal == null)
            throw new IllegalStateException("internal task has not been set yet");
        return internal;
    }

}
