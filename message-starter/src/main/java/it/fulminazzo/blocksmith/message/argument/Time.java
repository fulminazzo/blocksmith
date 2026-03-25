package it.fulminazzo.blocksmith.message.argument;

import it.fulminazzo.blocksmith.message.MessageParseContext;
import it.fulminazzo.blocksmith.message.argument.time.node.TimeNode;
import it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a timed placeholder replacement.
 * Replaces the given placeholder with the time based on the supplier.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Time implements Argument {
    private final @NotNull String placeholder;
    private final @NotNull String timeFormat;
    private final @NotNull Supplier<Long> timeSupplier;

    @Override
    public @NotNull Component apply(final @NotNull MessageParseContext context) {
        String format = timeFormat;
        Component tmp = context.getMessenger().getComponentOrNull(format, context.getLocale());
        if (tmp != null) format = ComponentUtils.toString(tmp);

        TimeParser parser = new TimeParser(format);
        TimeNode node = parser.parse();

        long time = timeSupplier.get();
        return Placeholder.of(placeholder, node.parse(time)).apply(context);
    }

    /**
     * Creates a new Time argument.
     * The placeholder will default to "<i>%time%</i>".
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param time the time
     * @return the time argument
     */
    public static @NotNull Time of(final long time) {
        return of(() -> time);
    }

    /**
     * Creates a new Time argument.
     * The placeholder will default to "<i>%time%</i>".
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Supplier<Long> timeSupplier) {
        return of("time", timeSupplier);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param placeholder the placeholder (without percentages)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final long time) {
        return of(ComponentUtils.toString(placeholder), time);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param placeholder the placeholder (without percentages)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final long time) {
        return of(placeholder, () -> time);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(ComponentUtils.toString(placeholder), timeSupplier);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code "<i>general.time-format</i>".
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(placeholder, "general.time-format", timeSupplier);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder the placeholder (without percentages)
     * @param timeFormat  the format to display the time to.
     *                    During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                    of the current context of action (meaning message codes are supported)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull Component timeFormat,
                                   final long time) {
        return of(ComponentUtils.toString(placeholder), ComponentUtils.toString(timeFormat), time);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder the placeholder (without percentages)
     * @param timeFormat  the format to display the time to.
     *                    During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                    of the current context of action (meaning message codes are supported)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull String timeFormat,
                                   final long time) {
        return of(ComponentUtils.toString(placeholder), timeFormat, time);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder the placeholder (without percentages)
     * @param timeFormat  the format to display the time to.
     *                    During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                    of the current context of action (meaning message codes are supported)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull Component timeFormat,
                                   final long time) {
        return of(placeholder, ComponentUtils.toString(timeFormat), time);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder the placeholder (without percentages)
     * @param timeFormat  the format to display the time to.
     *                    During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                    of the current context of action (meaning message codes are supported)
     * @param time        the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull String timeFormat,
                                   final long time) {
        return of(placeholder, timeFormat, () -> time);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeFormat   the format to display the time to.
     *                     During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                     of the current context of action (meaning message codes are supported)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull Component timeFormat,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(ComponentUtils.toString(placeholder), ComponentUtils.toString(timeFormat), timeSupplier);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeFormat   the format to display the time to.
     *                     During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                     of the current context of action (meaning message codes are supported)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull String timeFormat,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(ComponentUtils.toString(placeholder), timeFormat, timeSupplier);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeFormat   the format to display the time to.
     *                     During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                     of the current context of action (meaning message codes are supported)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull Component timeFormat,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(placeholder, ComponentUtils.toString(timeFormat), timeSupplier);
    }

    /**
     * Creates a new Time argument.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeFormat   the format to display the time to.
     *                     During application, the argument will try to fetch it from the {@link it.fulminazzo.blocksmith.message.Messenger}
     *                     of the current context of action (meaning message codes are supported)
     * @param timeSupplier the function to obtain the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull String timeFormat,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return new Time(placeholder, timeFormat, timeSupplier);
    }

}
