package it.fulminazzo.blocksmith.scheduler.bungee;

import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class BungeeTaskBuilder extends TaskBuilder {

    BungeeTaskBuilder(final @NotNull Plugin owner, final @NotNull Consumer<Task> function) {
        super(owner, function);
    }

    @Override
    protected @NotNull Task run(final @NotNull Object owner,
                                final @NotNull Consumer<Task> function) {
        BungeeTask task = new BungeeTask();
        Plugin plugin = (Plugin) owner;
        TaskScheduler scheduler = plugin.getProxy().getScheduler();

        if (delay == null && interval == null) {
            task.setInternal(scheduler.runAsync(plugin, () -> function.accept(task)));
        } else if (delay != null && interval == null) {
            task.setInternal(scheduler.schedule(plugin,
                    () -> function.accept(task),
                    delay.toMillis(),
                    TimeUnit.MILLISECONDS
            ));
        } else {
            task.setInternal(scheduler.schedule(plugin,
                    () -> function.accept(task),
                    delay == null ? 0 : delay.toMillis(),
                    interval.toMillis(),
                    TimeUnit.MILLISECONDS
            ));
        }
        return task;
    }

}
