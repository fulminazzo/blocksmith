package it.fulminazzo.blocksmith.message.provider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * An object to handle messages configuration migrations.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Migration {
    @Getter
    private final @NotNull Map<String, Object> data;
    private final @NotNull Map<String, Object> reference;

    /**
     * Reports the renaming and update of a message.
     *
     * @param from the path of the message
     * @param to   the new path of the message
     * @return this object (for method chaining)
     */
    public @NotNull Migration update(final @NotNull String from,
                                     final @NotNull String to) {
        return rename(from, to).update(to);
    }

    /**
     * Reports the renaming of a message.
     *
     * @param from the path of the message
     * @param to   the new path of the message
     * @return this object (for method chaining)
     */
    public @NotNull Migration rename(final @NotNull String from, final @NotNull String to) {
        Object object = data.remove(from);
        if (object == null) object = reference.get(to);
        data.put(to, object);
        return this;
    }

    /**
     * Reports the update of a message.
     *
     * @param path the path of the message
     * @return this object (for method chaining)
     */
    public @NotNull Migration update(final @NotNull String path) {
        data.put(path, reference.get(path));
        return this;
    }

    /**
     * Reports the addition of a message.
     *
     * @param path the path of the message
     * @return this object (for method chaining)
     */
    public @NotNull Migration add(final @NotNull String path) {
        /*
         * If the message is not present in the reference, we return null.
         * This is because we are assuming that:
         * 1. the message got later removed;
         * 2. the message got renamed, which case the rename method will handle accordingly.
         */
        data.put(path, reference.get(path));
        return this;
    }

    /**
     * Reports the removal of a message.
     *
     * @param path the path of the message
     * @return this object (for method chaining)
     */
    public @NotNull Migration remove(final @NotNull String path) {
        data.remove(path);
        return this;
    }

}
