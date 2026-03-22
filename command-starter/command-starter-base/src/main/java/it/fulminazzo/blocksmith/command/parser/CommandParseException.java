package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

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
     * <br>
     * The message will be formatted using the provided arguments.
     * <ul>
     *     <li>If an argument is a method, it will be formatted to be displayed properly.</li>
     *     <li>If an argument is a class, it will be replaced with its simple name.</li>
     * </ul>
     *
     * @param format the format of the message
     * @param args   the arguments
     * @return the command parse exception
     */
    public static @NotNull CommandParseException of(final @NotNull String format,
                                                    final @NotNull Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Method) args[i] = ReflectionUtils.methodToString((Method) arg);
            else if (arg instanceof Class) args[i] = ((Class<?>) arg).getSimpleName();
            else if (arg instanceof CommandToken) args[i] = ((CommandToken) arg).getToken();
        }
        return new CommandParseException(String.format(format, args));
    }

}
