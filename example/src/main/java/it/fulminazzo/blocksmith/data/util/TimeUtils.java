package it.fulminazzo.blocksmith.data.util;

import it.fulminazzo.blocksmith.function.RunnableException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {

    public static double time(final @NotNull RunnableException<Exception> task) {
        try {
            long curr = System.nanoTime();
            task.run();
            long time = System.nanoTime() - curr;
            double millis = time / 1_000_000.0;
            return Math.round(millis * 100.0) / 100.0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
