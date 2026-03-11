package it.fulminazzo.blocksmith.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A builder for {@link Task}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TaskBuilder {
    private final @NotNull Object owner;
    private final @NotNull Consumer<Task> function;

    protected @Nullable Duration delay;
    protected @Nullable Duration interval;

    protected boolean async;

    /**
     * Schedules the task for execution.
     *
     * @return the task
     */
    public @NotNull Task schedule() {
        return schedule(owner, function);
    }

    /**
     * Schedules the task for execution.
     *
     * @param owner    the owner of the task
     * @param function the function to execute
     * @return the task
     */
    protected abstract @NotNull Task schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function);

    /**
     * Specifies how much time to wait before executing the task.
     *
     * @param time the time
     * @param unit the unit
     * @return this object (for method chaining)
     */
    public @NotNull TaskBuilder delay(final @Range(from = 0, to = Long.MAX_VALUE) long time,
                                      final @NotNull TimeUnit unit) {
        return delay(Duration.of(time, unit.toChronoUnit()));
    }

    /**
     * Specifies how much time to wait before executing the task.
     *
     * @param delay the delay
     * @return this object (for method chaining)
     */
    public @NotNull TaskBuilder delay(final @NotNull Duration delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Specifies how often the task should be executed.
     *
     * @param time the time
     * @param unit the unit
     * @return this object (for method chaining)
     */
    public @NotNull TaskBuilder interval(final @Range(from = 0, to = Long.MAX_VALUE) long time,
                                         final @NotNull TimeUnit unit) {
        return interval(Duration.of(time, unit.toChronoUnit()));
    }

    /**
     * Specifies how often the task should be executed.
     *
     * @param interval the interval
     * @return this object (for method chaining)
     */
    public @NotNull TaskBuilder interval(final @NotNull Duration interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Specifies that the task should be run <b>asynchronously</b>.
     * <br>
     * <b>NOTE</b>: on some platforms this will be the default behavior.
     *
     * @return this object (for method chaining)
     */
    public @NotNull TaskBuilder async() {
        this.async = true;
        return this;
    }

}
