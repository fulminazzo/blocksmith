package it.fulminazzo.blocksmith.scheduler.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

final class VelocityTaskBuilder extends TaskBuilder {

    VelocityTaskBuilder(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        super(owner, function);
    }

    @Override
    protected @NotNull Task run(final @NotNull Object owner,
                                final @NotNull Consumer<Task> function) {
        VelocityTask task = new VelocityTask(owner);
        Scheduler scheduler = getServer(owner).getScheduler();
        Scheduler.TaskBuilder builder = scheduler.buildTask(owner, () -> function.accept(task));
        if (delay != null) builder.delay(delay);
        if (interval != null) builder.repeat(interval);
        task.setInternal(builder.schedule());
        return task;
    }

    private @NotNull ProxyServer getServer(final @NotNull Object plugin) {
        return Reflect.on(plugin)
                .get(f -> !Modifier.isStatic(f.getModifiers()) && ProxyServer.class.isAssignableFrom(f.getType()))
                .get();
    }

}
