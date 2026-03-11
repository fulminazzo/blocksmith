package it.fulminazzo.blocksmith.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MockTaskFactory implements TaskFactory {

    @Override
    public @NotNull TaskBuilder schedule(final @NotNull Object owner, final @NotNull Consumer<Task> function) {
        return new MockTaskBuilder(owner, function);
    }

    @Override
    public boolean supportsOwner(final @NotNull Class<?> ownerType) {
        return Long.class.isAssignableFrom(ownerType);
    }

}
