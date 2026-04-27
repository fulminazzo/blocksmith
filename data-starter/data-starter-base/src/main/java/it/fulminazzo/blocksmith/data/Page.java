package it.fulminazzo.blocksmith.data;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents the page of a query request.
 */
@Value(staticConstructor = "of")
public class Page {
    @Range(from = 0, to = Integer.MAX_VALUE)
    int number;
    @Range(from = 1, to = Integer.MAX_VALUE)
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

}
