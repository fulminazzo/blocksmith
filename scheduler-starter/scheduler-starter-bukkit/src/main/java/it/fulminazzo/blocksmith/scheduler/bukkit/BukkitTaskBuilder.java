package it.fulminazzo.blocksmith.scheduler.bukkit;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

final class BukkitTaskBuilder extends TaskBuilder {

    BukkitTaskBuilder(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        super(owner, function);
    }

    @Override
    protected @NotNull Task run(final @NotNull Object owner,
                                final @NotNull Consumer<Task> function) {
        BukkitTask task = new BukkitTask();
        Plugin plugin = (Plugin) owner;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        if (delay == null && interval == null) {
            if (async)
                task.setInternal(scheduler.runTaskAsynchronously(plugin, () -> function.accept(task)));
            else
                task.setInternal(scheduler.runTask(plugin, () -> function.accept(task)));
        } else if (delay != null && interval == null) {
            if (async)
                task.setInternal(scheduler.runTaskLaterAsynchronously(plugin,
                        () -> function.accept(task),
                        durationToTicks(delay)
                ));
            else
                task.setInternal(scheduler.runTaskLater(plugin,
                        () -> function.accept(task),
                        durationToTicks(delay)
                ));
        } else {
            if (async)
                task.setInternal(scheduler.runTaskTimerAsynchronously(plugin,
                        () -> function.accept(task),
                        durationToTicks(delay),
                        durationToTicks(interval)
                ));
            else
                task.setInternal(scheduler.runTaskTimer(plugin,
                        () -> function.accept(task),
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
