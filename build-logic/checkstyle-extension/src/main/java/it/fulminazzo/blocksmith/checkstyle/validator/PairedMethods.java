package it.fulminazzo.blocksmith.checkstyle.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PairedMethods {
    /*
     * GETTERS AND SETTERS
     */
    public static final @NotNull List<String> GETTERS = List.of("is", "get");
    public static final @NotNull List<String> SETTERS = List.of("set");

    /**
     * Checks if the method name starts with any of the given prefixes.
     *
     * @param methodName     the method name
     * @param firstPrefixes  the first prefixes
     * @param secondPrefixes the second prefixes
     * @return {@code true} if the method name starts with any of the given prefixes, {@code false} otherwise
     */
    public static boolean methodNameStartsWith(
            final @NotNull String methodName,
            final @NotNull List<String> firstPrefixes,
            final @NotNull List<String> secondPrefixes
    ) {
        return firstPrefixes.stream().anyMatch(methodName::startsWith)
                || secondPrefixes.stream().anyMatch(methodName::startsWith);
    }

}
