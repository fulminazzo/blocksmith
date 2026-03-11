package it.fulminazzo.blocksmith;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MockScheduler {
    public static @NotNull MockScheduler INSTANCE = new MockScheduler();

    private final @NotNull ScheduledExecutorService asyncExecutor = Executors.newSingleThreadScheduledExecutor();
    private final @NotNull Map<Long, Object> tasks = new ConcurrentHashMap<>();

    public void schedule(final long id,
                         final @NotNull Runnable task,
                         final @Nullable Long delayInMillis,
                         final @Nullable Long intervalInMillis,
                         final boolean async) {
        if (async) {
            if (delayInMillis == null && intervalInMillis == null)
                tasks.put(id, asyncExecutor.schedule(task, 0, TimeUnit.MILLISECONDS));
            else if (delayInMillis != null && intervalInMillis == null)
                tasks.put(id, asyncExecutor.schedule(task, delayInMillis, TimeUnit.MILLISECONDS));
            else {
                tasks.put(id, asyncExecutor.scheduleAtFixedRate(task,
                        delayInMillis == null ? 0 : delayInMillis,
                        intervalInMillis,
                        TimeUnit.MILLISECONDS
                ));
            }
        } else {
            try {
                if (delayInMillis == null && intervalInMillis == null) task.run();
                else if (delayInMillis != null && intervalInMillis == null) {
                    tasks.put(id, task);
                    Thread.sleep(delayInMillis);
                    if (!isCancelled(id)) {
                        task.run();
                        cancel(id);
                    }
                } else {
                    tasks.put(id, task);
                    if (delayInMillis != null) Thread.sleep(delayInMillis);
                    while (!isCancelled(id)) {
                        task.run();
                        Thread.sleep(intervalInMillis);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isCancelled(final long id) {
        Object task = tasks.get(id);
        if (task instanceof ScheduledFuture) return ((ScheduledFuture<?>) task).isCancelled();
        return task == null;
    }

    public void cancel(final long id) {
        Object task = tasks.remove(id);
        if (task instanceof ScheduledFuture) ((ScheduledFuture<?>) task).cancel(true);
    }

}
