package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * An object to handle configuration migrations.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Migration {
    @Getter
    private final @NotNull Map<String, Object> data;

    /**
     * Renames a property.
     *
     * @param from the name of the property
     * @param to   the new name of the property
     * @return this object (for method chaining)
     */
    public @NotNull Migration rename(final @NotNull String from, final @NotNull String to) {
        return update(from, to, data.get(from));
    }

    /**
     * Updates the value of a property.
     *
     * @param path  the path of the property
     * @param value the new value
     * @return this object (for method chaining)
     */
    public @NotNull Migration update(final @NotNull String path, final @NotNull Object value) {
        return update(path, path, value);
    }

    /**
     * Renames and updates the value of a property.
     *
     * @param from  the name of the property
     * @param to    the new name of the property
     * @param value the new value
     * @return this object (for method chaining)
     */
    public @NotNull Migration update(final @NotNull String from,
                                     final @NotNull String to,
                                     final @NotNull Object value) {
        return remove(from).add(to, value);
    }

    /**
     * Adds a new property.
     *
     * @param path  the path of the property
     * @param value the new value
     * @return this object (for method chaining)
     */
    public @NotNull Migration add(final @NotNull String path, final @NotNull Object value) {
        if (data.containsKey(path)) throw new IllegalArgumentException(String.format("Path '%s' already exists", path));
        data.put(path, value);
        return this;
    }

    /**
     * Removes a property.
     *
     * @param path the path of the property
     * @return this object (for method chaining)
     */
    public @NotNull Migration remove(final @NotNull String path) {
        if (data.remove(path) == null)
            throw new IllegalArgumentException(String.format("Path '%s' does not exist", path));
        return this;
    }

}
