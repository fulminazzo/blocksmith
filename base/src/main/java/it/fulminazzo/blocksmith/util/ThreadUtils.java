package it.fulminazzo.blocksmith.util;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * A collection of utilities for working with threads.
 */
public final class ThreadUtils {
    private static final @NotNull Map<Class<?>, Integer> THREADS_COUNT = new ConcurrentHashMap<>();

    /**
     * Creates a thread factory for the given owner.
     * Factories are shared among owners of the same type, meaning that the thread naming and count will be the same.
     *
     * @param owner the owner of the thread
     * @return the thread factory
     */
    public static @NotNull ThreadFactory ownedThreadFactory(final @NotNull Class<?> owner) {
        return ownedThreadFactory(owner, false, null);
    }

    /**
     * Creates a thread factory for the given owner.
     * Factories are shared among owners of the same type, meaning that the thread naming and count will be the same.
     *
     * @param owner  the owner of the thread
     * @param daemon if {@code true} the thread will be a daemon thread
     * @param suffix the suffix to append in the thread names after the owner name and before the thread count
     * @return the thread factory
     */
    public static @NotNull ThreadFactory ownedThreadFactory(
            final @NotNull Class<?> owner,
            final boolean daemon,
            final @Nullable String suffix
    ) {
        return r -> {
            Thread thread = new Thread(r);
            thread.setName(getThreadName(owner, suffix));
            thread.setDaemon(daemon);
            return thread;
        };
    }

    /**
     * Gets a thread name for the given owner using the uniform format across the entire <b>blocksmith</b> module.
     * <br>
     * The format is the following: {@code blocksmith-<owner_name>(-<suffix>)-<thread_count>}
     *
     * @param owner  the owner of the thread
     * @param suffix the suffix to append after the owner name and before the thread count
     * @return the thread name
     */
    static @NotNull String getThreadName(final @NotNull Class<?> owner, final @Nullable String suffix) {
        final StringBuilder name = new StringBuilder(ProjectInfo.PROJECT_NAME)
                .append("-")
                .append(CaseConverter.convert(
                        owner.getSimpleName(),
                        Convention.PASCAL_CASE,
                        Convention.KEBAB_CASE)
                ).append("-");
        if (suffix != null) name.append(suffix).append("-");
        return name
                .append(THREADS_COUNT.compute(owner, (k, v) -> v == null ? 0 : v + 1))
                .toString();
    }

}
