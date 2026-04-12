package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.reflect.ReflectException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown when the parsing of a command fails.
 */
public final class CommandParseException extends RuntimeException {

    /**
     * Instantiates a new Command parse exception.
     *
     * @param message the message
     */
    public CommandParseException(final @NotNull String message) {
        super(message);
    }

    /**
     * Instantiates a new Command parse exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CommandParseException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Command parse exception.
     *
     * @param format the format of the message
     * @param args   the arguments
     * @return the command parse exception
     */
    public static @NotNull CommandParseException of(final @NotNull String format,
                                                    final @Nullable Object @NotNull ... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Class) args[i] = ((Class<?>) arg).getSimpleName();
            else if (arg instanceof CommandToken) args[i] = ((CommandToken) arg).getToken();
        }
        return new CommandParseException(ReflectException.formatMessage(format, args));
    }

}
