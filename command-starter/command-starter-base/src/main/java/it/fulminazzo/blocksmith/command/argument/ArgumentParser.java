package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.TabCompletable;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A parser to convert raw input to a Java type.
 *
 * @param <T> the type of the parsed argument
 */
public interface ArgumentParser<T> extends TabCompletable {

    /**
     * Attempts to advance the visitor cursor past the argument node associated with this parser.
     *
     * @param visitor the current visitor
     * @return {@code true} if the current input was validated and the cursor was advanced, {@code false} otherwise
     */
    default boolean tryAdvanceCursor(final @NotNull Visitor<?, ?> visitor) {
        try {
            parse(visitor);
            return true;
        } catch (ArgumentParseException e) {
            return false;
        }
    }

    /**
     * Parses the given string.
     *
     * @param visitor the visitor containing the current input and other useful data
     * @return the object
     * @throws ArgumentParseException in case of parsing errors
     */
    @Nullable T parse(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException;

}
