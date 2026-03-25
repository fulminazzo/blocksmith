package it.fulminazzo.blocksmith.message.argument.time.parser;

import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when the parsing of a time format fails.
 */
public final class TimeParseException extends RuntimeException {

    /**
     * Instantiates a new Time parse exception.
     *
     * @param message the message
     */
    public TimeParseException(final @NotNull String message) {
        super(message);
    }

    /**
     * Instantiates a new Time parse exception.
     *
     * @param format the format of the message
     * @param args   the arguments
     * @return the time parse exception
     */
    public static @NotNull TimeParseException of(final @NotNull String format,
                                                 final @NotNull Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof TimeToken) args[i] = ((TimeToken) arg).getToken();
        }
        return new TimeParseException(String.format(format, args));
    }

}
