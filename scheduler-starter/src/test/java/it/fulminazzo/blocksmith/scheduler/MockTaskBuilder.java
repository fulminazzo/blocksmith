package it.fulminazzo.blocksmith.scheduler;

import it.fulminazzo.blocksmith.MockScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class MockTaskBuilder extends TaskBuilder {

    MockTaskBuilder(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        super(owner, function);
    }

    @Override
    protected @NotNull Task build(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        MockTask task = new MockTask((Long) owner, async);
        MockScheduler.INSTANCE.schedule(
                task.getOwner(),
                () -> function.accept(task),
                delay == null ? null : delay.toMillis(),
                interval == null ? null : interval.toMillis(),
                async
        );
        return task;
    }

}
