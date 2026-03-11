package it.fulminazzo.blocksmith.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class MockTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        return new MockTaskBuilder(owner, function);
    }

}
