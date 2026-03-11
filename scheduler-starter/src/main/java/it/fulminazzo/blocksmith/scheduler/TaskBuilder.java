package it.fulminazzo.blocksmith.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
     * Schedules the task for execution, then executes it for the specified times.
     * <br>
     * If {@link #delay} has not been set, this will have no effect.
     *
     * @param times how many times the task should run before stopping
     * @return the task
     */
    public @NotNull Task repeated(final @Range(from = 1, to = Integer.MAX_VALUE) int times) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        return repeated(t -> atomicInteger.incrementAndGet() > times);
    }

    /**
     * Schedules the task for execution, then executes it until a certain condition is met.
     * <br>
     * If {@link #delay} has not been set, this will have no effect.
     *
     * @param condition the condition to check
     * @return the task
     */
    public @NotNull Task repeated(final @NotNull Predicate<Task> condition) {
        return run(owner, t -> {
            if (condition.test(t)) t.cancel();
            else function.accept(t);
        });
    }

    /**
     * Schedules the task for execution.
     *
     * @return the task
     */
    public @NotNull Task run() {
        return run(owner, function);
    }

    /**
     * Schedules the task for execution.
     *
     * @param owner    the owner of the task
     * @param function the function to execute
     * @return the task
     */
    protected abstract @NotNull Task run(final @NotNull Object owner, final @NotNull Consumer<Task> function);

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
        if (delay.isNegative()) throw new IllegalArgumentException("Invalid delay: must be at least zero");
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
        if (interval.isNegative()) throw new IllegalArgumentException("Invalid delay: must be at least zero");
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
