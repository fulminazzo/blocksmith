package it.fulminazzo.blocksmith.scheduler.bukkit;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import it.fulminazzo.blocksmith.structure.Pair;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class BukkitTaskFactory implements TaskFactory {
    private final @Nullable TaskFactory delegate;

    public BukkitTaskFactory() {
        TaskFactory delegate = null;
        try {
            // attempt to load Folia task factory
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            delegate = Reflect.on(BukkitTaskFactory.class.getCanonicalName()
                    .replace("bukkit", "folia")
                    .replace("Bukkit", "Folia")
            ).get();
        } catch (ClassNotFoundException t) {
            // not on Folia or modern Paper, fallback to default
        }
        this.delegate = delegate;
    }

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        if (delegate != null) return delegate.schedule(owner, function);
        else if (owner instanceof Pair<?, ?>) return schedule(((Pair<?, ?>) owner).getFirst(), function);
        else if (supportsOwner(owner.getClass())) return new BukkitTaskBuilder(owner, function);
        else throw new IllegalArgumentException(String.format("%s does not support owner type: %s",
                    BukkitTask.class.getSimpleName(), owner.getClass().getCanonicalName()
            ));
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        if (delegate != null) return delegate.supportsOwner(ownerType);
        else if (Pair.class.isAssignableFrom(ownerType)) return true;
        else return Plugin.class.isAssignableFrom(ownerType);
    }

}
