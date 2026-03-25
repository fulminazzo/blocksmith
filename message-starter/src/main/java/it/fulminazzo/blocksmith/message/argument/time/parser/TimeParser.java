package it.fulminazzo.blocksmith.message.argument.time.parser;

import it.fulminazzo.blocksmith.structure.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

final class TimeParser {
    private final @NotNull String timeFormat;
    private final @NotNull TimeTokenizer tokenizer;

    /**
     * Instantiates a new Time parser.
     *
     * @param timeFormat the time format
     */
    TimeParser(final @NotNull String timeFormat) {
        this.timeFormat = timeFormat;
        this.tokenizer = new TimeTokenizer(timeFormat);
    }

    /**
     * SINGULAR_AND_PLURAL := { {@link TimeToken#TEXT} \| {@link TimeToken#TEXT} }
     *
     * @return the parsed singular and plurals
     */
    @NotNull Pair<String, String> parseSingularAndPlural() {
        consume(TimeToken.OPEN_BRACE);
        String singular = tokenizer.getLastRead();
        consume(TimeToken.TEXT);
        consume(TimeToken.PIPE);
        String plural = tokenizer.getLastRead();
        consume(TimeToken.TEXT);
        consume(TimeToken.CLOSE_BRACE);
        return Pair.of(singular, plural);
    }

    /**
     * Checks if the given token matches with the last read one.
     * <br>
     * If it does, the next one is read.
     *
     * @param expected the expected token
     */
    void consume(final @NotNull TimeToken expected) {
        match(expected);
        tokenizer.next();
    }

    /**
     * Checks if the given token matches with the last read one.
     *
     * @param expected the expected token
     */
    void match(final @NotNull TimeToken expected) {
        if (tokenizer.getLastToken() != expected)
            throw parseException("expected '%s' but got '%s'", expected, tokenizer.getLastRead());
    }

    private @NotNull TimeParseException parseException(final @NotNull String message,
                                                       final Object @NotNull ... args) {
        return TimeParseException.of("Invalid input in time format '%s': " + message,
                Stream.concat(Stream.of(timeFormat), Stream.of(args)).toArray()
        );
    }

}
