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
     * Checks if the method is prefixed with any of the given prefixes.
     *
     * @param methodName the method name
     * @param prefixes   the prefixes
     */
    public static boolean isMethodPairedWith(final @NotNull String methodName, final @NotNull List<String> prefixes) {
        return prefixes.stream().anyMatch(methodName::startsWith);
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
     * Gets all the all adders and all removers.
     *
     * @return the all adders and all removers
     */
    public static @NotNull List<String> getAllAddersAndRemovers() {
        List<String> allAddersAndRemovers = new ArrayList<>();
        allAddersAndRemovers.addAll(ALL_ADDERS);
        allAddersAndRemovers.addAll(ALL_REMOVERS);
        return allAddersAndRemovers;
    }

    /**
     * Gets all the registerers and unregisterers.
     *
     * @return the registerers and unregisterers
     */
    public static @NotNull List<String> getRegisterersAndUnregisterers() {
        List<String> registerersAndUnregisterers = new ArrayList<>();
        registerersAndUnregisterers.addAll(REGISTERERS);
        registerersAndUnregisterers.addAll(UNREGISTERERS);
        return registerersAndUnregisterers;
    }

    /**
     * Gets all the adders and removers.
     *
     * @return the adders and removers
     */
    public static @NotNull List<String> getAddersAndRemovers() {
        List<String> addersAndRemovers = new ArrayList<>();
        addersAndRemovers.addAll(ADDERS);
        addersAndRemovers.addAll(REMOVERS);
        return addersAndRemovers;
    }

    /**
     * Gets all the getters and setters.
     *
     * @return the getters and setters
     */
    public static @NotNull List<String> getGettersAndSetters() {
        List<String> gettersAndSetters = new ArrayList<>();
        gettersAndSetters.addAll(GETTERS);
        gettersAndSetters.addAll(SETTERS);
        return gettersAndSetters;
    }

}
