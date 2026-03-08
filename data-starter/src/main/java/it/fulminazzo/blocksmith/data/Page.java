package it.fulminazzo.blocksmith.data;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents the page of a query request.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Page {
    int number;
    int size;

    /**
     * Gets the first page.
     *
     * @return the page
     */
    public @NotNull Page first() {
        return new Page(0, size);
    }

    /**
     * Gets the next page (unchecked).
     *
     * @return the page
     */
    public @NotNull Page next() {
        return new Page(number + 1, size);
    }

    /**
     * Gets the previous page (or current if it is the {@link #first()}).
     *
     * @return the page
     */
    public @NotNull Page previous() {
        if (number > 0) return new Page(number - 1, size);
        else return this;
    }

    /**
     * Creates a new Page.
     *
     * @param number the number of the page
     * @param size   the size of the pages
     * @return the page
     */
    public static @NotNull Page of(final @Range(from = 0, to = Integer.MAX_VALUE) int number,
                                   final @Range(from = 1, to = Integer.MAX_VALUE) int size) {
        return new Page(number, size);
    }

}
