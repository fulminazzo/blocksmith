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
        return new FoliaTaskBuilder((Plugin) owner, function);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return Plugin.class.isAssignableFrom(ownerType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
