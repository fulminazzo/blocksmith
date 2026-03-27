package it.fulminazzo.blocksmith.scheduler.folia;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import it.fulminazzo.blocksmith.structure.Pair;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class FoliaTaskBuilder extends TaskBuilder {

    FoliaTaskBuilder(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        super(owner, function);
    }

    @Override
    protected @NotNull Task run(final @NotNull Object owner,
                                final @NotNull Consumer<Task> function) {
        FoliaTask task = new FoliaTask(async);
        Plugin plugin = getPlugin(owner);
        Server server = plugin.getServer();

        if (async) {
            @NotNull AsyncScheduler scheduler = server.getAsyncScheduler();
            if (delay == null && interval == null)
                task.setInternal(scheduler.runNow(plugin, t -> function.accept(task)));
            else if (delay != null && interval == null)
                task.setInternal(scheduler.runDelayed(plugin,
                        t -> function.accept(task),
                        delay.toMillis(),
                        TimeUnit.MILLISECONDS
                ));
            else
                task.setInternal(scheduler.runAtFixedRate(plugin,
                        t -> function.accept(task),
                        delay == null ? 0 : delay.toMillis(),
                        interval.toMillis(),
                        TimeUnit.MILLISECONDS
                ));
        } else {
            if (owner instanceof Pair<?, ?>) {
                Object actualOwner = ((Pair<?, ?>) owner).getSecond();
                if (actualOwner instanceof Location) {
                    Location location = (Location) actualOwner;
                    RegionScheduler scheduler = server.getRegionScheduler();
                    if (delay == null && interval == null)
                        task.setInternal(scheduler.run(plugin, location, t -> function.accept(task)));
                    else if (delay != null && interval == null)
                        task.setInternal(scheduler.runDelayed(plugin,
                                location,
                                t -> function.accept(task),
                                durationToTicks(delay)
                        ));
                    else
                        task.setInternal(scheduler.runAtFixedRate(plugin,
                                location,
                                t -> function.accept(task),
                                durationToTicks(delay),
                                durationToTicks(interval)
                        ));
                    return task;
                } else if (actualOwner instanceof Entity) {
                    Entity entity = (Entity) actualOwner;
                    EntityScheduler scheduler = entity.getScheduler();
                    if (delay == null && interval == null)
                        task.setInternal(scheduler.run(plugin, t -> function.accept(task), () -> {
                        }));
                    else if (delay != null && interval == null)
                        task.setInternal(scheduler.runDelayed(plugin,
                                t -> function.accept(task),
                                () -> {
                                },
                                durationToTicks(delay)
                        ));
                    else
                        task.setInternal(scheduler.runAtFixedRate(plugin,
                                t -> function.accept(task),
                                () -> {
                                },
                                durationToTicks(delay),
                                durationToTicks(interval)
                        ));
                    return task;
                }
            }
            GlobalRegionScheduler scheduler = server.getGlobalRegionScheduler();
            if (delay == null && interval == null)
                task.setInternal(scheduler.run(plugin, t -> function.accept(task)));
            else if (delay != null && interval == null)
                task.setInternal(scheduler.runDelayed(plugin,
                        t -> function.accept(task),
                        durationToTicks(delay)
                ));
            else
                task.setInternal(scheduler.runAtFixedRate(plugin,
                        t -> function.accept(task),
                        durationToTicks(delay),
                        durationToTicks(interval)
                ));
        }

        return task;
    }

    private long durationToTicks(final @Nullable Duration duration) {
        return duration == null ? 0 : duration.toMillis() / 50;
    }

    private static @NotNull Plugin getPlugin(@NotNull Object owner) {
        final Object pluginObject;
        if (owner instanceof Pair<?, ?>) pluginObject = ((Pair<?, ?>) owner).getFirst();
        else pluginObject = owner;

        final Plugin plugin;
        if (pluginObject instanceof Plugin) plugin = (Plugin) pluginObject;
        else throw new IllegalArgumentException(String.format("Invalid owner type '%s': expected %s",
                pluginObject == null ? "null" : pluginObject.getClass().getCanonicalName(),
                Plugin.class.getCanonicalName())
        );
        return plugin;
    }

}
