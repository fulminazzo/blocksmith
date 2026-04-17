package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.ApplicationHandle;
import it.fulminazzo.blocksmith.ServerApplication;
import it.fulminazzo.blocksmith.message.Messenger;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Value
@AllArgsConstructor
public class MockApplicationHandle implements ApplicationHandle, ServerApplication {
    private static final @NotNull ExecutorService executor = Executors.newSingleThreadExecutor();

    @NotNull Messenger messenger;
    @NotNull Logger log;

    public MockApplicationHandle() {
        this.messenger = new Messenger(this);
        this.log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public @NotNull ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public @NotNull Object getServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull <S> S server() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull <T> T as(@NotNull Class<T> type) {
        return type.cast(this);
    }

    @Override
    public @NotNull Logger logger() {
        return log;
    }

    @Override
    public @NotNull String getName() {
        return "blocksmith";
    }

}
