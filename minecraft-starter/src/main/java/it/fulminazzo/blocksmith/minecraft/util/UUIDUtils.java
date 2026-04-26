package it.fulminazzo.blocksmith.minecraft.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A collection of utilities to work with {@link UUID}s.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UUIDUtils {

    /**
     * Converts the given UUID to its undashed form.
     *
     * @param uuid the uuid
     * @return the undashed uuid
     */
    public static @NotNull String undashed(final @NotNull UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    /**
     * Converts the given undashed UUID to its dashed form.
     *
     * @param uuid the uuid
     * @return the dashed uuid
     */
    public static @NotNull UUID dashed(final @NotNull String uuid) {
        String dashed = uuid.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(dashed);
    }

}
