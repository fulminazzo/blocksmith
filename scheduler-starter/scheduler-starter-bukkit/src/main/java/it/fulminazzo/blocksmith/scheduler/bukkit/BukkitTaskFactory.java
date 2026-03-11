package it.fulminazzo.blocksmith.scheduler.bukkit;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import it.fulminazzo.blocksmith.structure.Pair;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class BukkitTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        if (owner instanceof Pair<?, ?>) return schedule(((Pair<?, ?>) owner).getFirst(), function);
        else if (supportsOwner(owner.getClass())) return new BukkitTaskBuilder(owner, function);
        else throw new IllegalArgumentException(String.format("%s does not support owner type: %s",
                    BukkitTask.class.getSimpleName(), owner.getClass().getCanonicalName()
            ));
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return Plugin.class.isAssignableFrom(ownerType);
    }

}
