package it.fulminazzo.blocksmith.scheduler.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import it.fulminazzo.blocksmith.scheduler.Task;
import it.fulminazzo.blocksmith.scheduler.TaskBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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
        final String errorMessage = String.format("Could not find a %s field in %s",
                ProxyServer.class.getSimpleName(), plugin.getClass().getCanonicalName());
        try {
            Field field = Arrays.stream(plugin.getClass().getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()))
                    .filter(f -> ProxyServer.class.isAssignableFrom(f.getType()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException(errorMessage + ". Please specify one before scheduling any task"));
            field.setAccessible(true);
            return (ProxyServer) field.get(plugin);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

}
