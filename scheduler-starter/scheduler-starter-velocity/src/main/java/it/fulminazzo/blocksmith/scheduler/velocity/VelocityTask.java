package it.fulminazzo.blocksmith.scheduler.velocity;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;
import it.fulminazzo.blocksmith.scheduler.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
final class VelocityTask implements Task {
    @Getter
    private final @NotNull Object owner;

    @Setter
    private @Nullable ScheduledTask internal;

    @Override
    public void cancel() {
        getInternal().cancel();
    }

    @Override
    public boolean isCancelled() {
        return getInternal().status() == TaskStatus.CANCELLED;
    }

    @Override
    public boolean isAsync() {
        return true; // always asynchronous in Velocity
    }

    private @NotNull ScheduledTask getInternal() {
        if (internal == null)
            throw new IllegalStateException("internal task has not been set yet");
        return internal;
    }

}
