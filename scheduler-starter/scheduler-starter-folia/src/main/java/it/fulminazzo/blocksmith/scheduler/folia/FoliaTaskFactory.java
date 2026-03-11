package it.fulminazzo.blocksmith.scheduler.folia;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class FoliaTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        if (supportsOwner(owner.getClass())) return new FoliaTaskBuilder(owner, function);
        else throw new IllegalArgumentException(String.format("%s does not support owner type: %s",
                FoliaTask.class.getSimpleName(), owner.getClass().getCanonicalName()
        ));
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return Plugin.class.isAssignableFrom(ownerType);
    }

}
