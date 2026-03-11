package it.fulminazzo.blocksmith.scheduler;

import it.fulminazzo.blocksmith.MockScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class MockTask implements Task {
    private final long owner;
    @Getter
    private final boolean async;

    @Override
    public void cancel() {
        MockScheduler.INSTANCE.cancel(owner);
    }

    @Override
    public boolean isCancelled() {
        return MockScheduler.INSTANCE.isCancelled(owner);
    }

    @Override
    public @NotNull Object getOwner() {
        return owner;
    }

}
