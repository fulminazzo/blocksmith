package it.fulminazzo.blocksmith.scheduler.folia;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import org.bukkit.Server;
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
        Plugin plugin = (Plugin) owner;
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

}
