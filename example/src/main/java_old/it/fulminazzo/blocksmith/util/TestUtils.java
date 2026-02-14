package it.fulminazzo.blocksmith.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

    public static void assertEquals(final Object actual,
                                    final Object expected,
                                    final @NotNull String message) {
        if (!Objects.equals(expected, actual))
            throw new IllegalArgumentException(message + String.format("\n%s != %s", expected, actual));
    }

}
