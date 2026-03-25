package it.fulminazzo.blocksmith.message.argument.time.parser;

import it.fulminazzo.blocksmith.message.argument.time.node.ArgumentNode;
import it.fulminazzo.blocksmith.message.argument.time.node.LiteralNode;
import it.fulminazzo.blocksmith.message.argument.time.node.TimeNode;
import it.fulminazzo.blocksmith.structure.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * The type Time parser.
 */
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
     * EXPRESSION := OPTIONAL_ARGUMENT | ALWAYS_SHOWN_ARGUMENT | {@link TimeToken#TEXT}
     *
     * @return the node
     */
    @NotNull TimeNode parseExpression() {
        switch (tokenizer.getLastToken()) {
            case OPEN_BRACKET: return parseOptionalArgument();
            case OPEN_PHARENTHESIS: return parseAlwaysShownArgument();
            default: return new LiteralNode(tokenizer.getLastRead());
        }
    }

    /**
     * OPTIONAL_ARGUMENT := [ GENERAL_ARGUMENT ]
     *
     * @return the node
     */
    @NotNull TimeNode parseOptionalArgument() {
        consume(TimeToken.OPEN_BRACKET);
        ArgumentNode node = parseGeneralArgument(TimeToken.CLOSE_BRACKET);
        node.setOptional(true);
        consume(TimeToken.CLOSE_BRACKET);
        return node;
    }

    /**
     * ALWAYS_SHOWN_ARGUMENT := ( GENERAL_ARGUMENT )
     *
     * @return the node
     */
    @NotNull TimeNode parseAlwaysShownArgument() {
        consume(TimeToken.OPEN_PHARENTHESIS);
        ArgumentNode node = parseGeneralArgument(TimeToken.CLOSE_PARENTHESIS);
        consume(TimeToken.CLOSE_PARENTHESIS);
        return node;
    }

    /**
     * GENERAL_ARGUMENT := ( SINGULAR_AND_PLURAL | {@link TimeToken#TEXT} )* UNIT_PLACEHOLDER ( SINGULAR_AND_PLURAL | {@link TimeToken#TEXT} )*
     *
     * @param closeToken the token upon which the reading should stop
     * @return the node
     */
    @NotNull ArgumentNode parseGeneralArgument(final @NotNull TimeToken closeToken) {
        StringBuilder text = new StringBuilder();
        ArgumentNode.TimeUnit unit = null;
        Pair<String, String> singularAndPlural = null;
        Boolean full = null;

        TimeToken lastRead;
        while ((lastRead = tokenizer.getLastToken()) != closeToken && lastRead != TimeToken.EOF) {
            if (full == null) {
                if (lastRead == TimeToken.EXCLAMATION_MARK) {
                    full = true;
                    tokenizer.next();
                    continue;
                } else full = false;
            }
            if (lastRead == TimeToken.PERCENTAGE) {
                if (unit == null) {
                    unit = parseUnitPlaceholder();
                    text.append("%unit%");
                }
                else throw parseException("invalid argument '%s': unit was already specified", text);
            } else if (lastRead == TimeToken.OPEN_BRACE) {
                singularAndPlural = parseSingularAndPlural();
                text.append("%name%");
            } else {
                text.append(tokenizer.getLastRead());
                tokenizer.next();
            }
        }

        if (unit == null)
            throw parseException("no time unit was specified for argument '%s'. Please use %%<unit>%%", text);
        if (singularAndPlural == null) {
            String first = unit.getName().charAt(0) + "";
            singularAndPlural = Pair.of(first, first);
        }

        return new ArgumentNode(
                text.toString(),
                unit,
                singularAndPlural.getFirst(),
                singularAndPlural.getSecond(),
                full
        );
    }

    /**
     * UNIT_PLACEHOLDER := % {@link ArgumentNode.TimeUnit#getName()} %
     */
    @NotNull ArgumentNode.TimeUnit parseUnitPlaceholder() {
        consume(TimeToken.PERCENTAGE);
        match(TimeToken.TEXT);
        String rawUnit = tokenizer.getLastRead();
        ArgumentNode.TimeUnit unit = ArgumentNode.TimeUnit.of(rawUnit);
        if (unit == null) throw parseException("unknown time unit '%s'", rawUnit);
        consume(TimeToken.TEXT);
        consume(TimeToken.PERCENTAGE);
        return unit;
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
