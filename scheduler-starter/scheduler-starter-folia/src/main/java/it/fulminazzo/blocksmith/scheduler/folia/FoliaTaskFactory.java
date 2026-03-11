package it.fulminazzo.blocksmith.scheduler.folia;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.scheduler.TaskFactory;
import it.fulminazzo.blocksmith.structure.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

public final class FoliaTaskFactory implements TaskFactory {
    private static final @NotNull Set<Class<?>> supportedTypes = Set.of(
            Plugin.class, Location.class, Entity.class
    );

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        final String errorMessage = "%s does not support owner type: %s";
        if (owner instanceof Pair<?, ?>) {
            Pair<?, ?> pair = (Pair<?, ?>) owner;
            Object actualOwner = pair.getSecond();
            if (supportedTypes.stream().anyMatch(type -> type.isAssignableFrom(actualOwner.getClass())))
                return new FoliaTaskBuilder(owner, function);
            else throw new IllegalArgumentException(String.format(errorMessage,
                    FoliaTask.class.getSimpleName(),
                    actualOwner.getClass().getCanonicalName()
            ));
        }
        if (supportsOwner(owner.getClass())) return new FoliaTaskBuilder(owner, function);
        else throw new IllegalArgumentException(String.format(errorMessage,
                FoliaTask.class.getSimpleName(), owner.getClass().getCanonicalName()
        ));
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            if (Pair.class.isAssignableFrom(ownerType)) return true;
            else return Plugin.class.isAssignableFrom(ownerType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
