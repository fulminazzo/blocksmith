package it.fulminazzo.blocksmith.checkstyle.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PairedMethods {
    /*
     * ALL ADDERS AND REMOVERS
     */
    public static final @NotNull List<String> ALL_ADDERS = List.of("addAll", "appendAll", "putAll");
    public static final @NotNull List<String> ALL_REMOVERS = List.of("removeAll", "deleteAll");
    /**
     * REGISTERERS AND UNREGISTERERS
     */
    public static final @NotNull List<String> REGISTERERS = List.of("register");
    public static final @NotNull List<String> UNREGISTERERS = List.of("unregister");
    /*
     * ADDERS AND REMOVERS
     */
    public static final @NotNull List<String> ADDERS = List.of("add", "append", "put");
    public static final @NotNull List<String> REMOVERS = List.of("remove", "delete");
    /*
     * GETTERS AND SETTERS
     */
    public static final @NotNull List<String> GETTERS = List.of("is", "get");
    public static final @NotNull List<String> SETTERS = List.of("set");

    /**
     * Checks if the method is prefixed with any of the prefixes in this class.
     */
    public static boolean isMethodPaired(final @NotNull String methodName) {
        return getAll().stream().anyMatch(methodName::startsWith);
    }

    /**
     * Gets all the pairs of prefixes.
     *
     * @return a list with arrays of two elements representing the first and second prefixes
     */
    @SuppressWarnings("unchecked")
    public static @NotNull List<List<String>[]> getPairs() {
        List<List<String>[]> pairs = new ArrayList<>();
        pairs.add(new List[]{ALL_ADDERS, ALL_REMOVERS});
        pairs.add(new List[]{REGISTERERS, UNREGISTERERS});
        pairs.add(new List[]{ADDERS, REMOVERS});
        pairs.add(new List[]{GETTERS, SETTERS});
        return pairs;
    }

    /**
     * Gets all the prefixes.
     *
     * @return the prefixes
     */
    public static @NotNull List<String> getAll() {
        List<String> all = new ArrayList<>();
        all.addAll(ALL_ADDERS);
        all.addAll(ALL_REMOVERS);
        all.addAll(REGISTERERS);
        all.addAll(UNREGISTERERS);
        all.addAll(ADDERS);
        all.addAll(REMOVERS);
        all.addAll(GETTERS);
        all.addAll(SETTERS);
        return all;
    }

}
