package it.fulminazzo.blocksmith.scheduler.velocity;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class VelocityTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        return new VelocityTaskBuilder(owner, function);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return true; // any object can be a plugin, checks will be performed later
    }

}
