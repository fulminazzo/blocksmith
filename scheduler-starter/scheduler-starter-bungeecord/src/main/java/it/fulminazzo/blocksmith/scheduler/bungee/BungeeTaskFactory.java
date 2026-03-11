package it.fulminazzo.blocksmith.scheduler.bungee;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class BungeeTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        return new BungeeTaskBuilder((Plugin) owner, function);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return Plugin.class.isAssignableFrom(ownerType);
    }

}
