package it.fulminazzo.blocksmith.message.argument;

import it.fulminazzo.blocksmith.message.MessageParseContext;
import it.fulminazzo.blocksmith.message.argument.time.node.TimeNode;
import it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a timed placeholder replacement.
 * Replaces the given placeholder with the time based on the supplier.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Time implements Argument {
    /**
     * This is the default <b>message code</b> used to format a given time.
     * The code represents an entry translatable with a {@link it.fulminazzo.blocksmith.message.provider.MessageProvider}
     * to an actual format to allow for easier customization from the end user.
     * <br>
     * The supported format includes:
     * <ul>
     *     <li>{@code [<some_text>%unit%<some_other_text>{singular|plural}<some_other_text>]} to display
     *     the time in the given unit if greater than {@code 0}.
     *     For example, {@code [%seconds% {secs|sec}]} will display
     *     {@code 3000} as {@code 3 secs} and {@code 1000} as {@code 1 sec};</li>
     *     <li>{@code (<some_text>%unit%<some_other_text>{singular|plural}<some_other_text>)} to <b>always</b>
     *     display the time in the given unit, regardless if it is {@code 0};</li>
     *     <li>{@code !} to signal to report the full time unit.
     *     For example, {@code [!%seconds%]} (but works with {@code ( )} as well) of {@code 62000}
     *     will print {@code 62} rather than {@code 2}.</li>
     * </ul>
     */
    public static final @NotNull String DEFAULT_FORMAT = "general.time-format";

    @NotNull String placeholder;
    @NotNull String timeFormat;
    @NotNull Supplier<Long> timeSupplier;

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
     * The format will be the message code {@link #DEFAULT_FORMAT}.
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
     * The format will be the message code {@link #DEFAULT_FORMAT}.
     *
     * @param timeSupplier the function to get the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Supplier<Long> timeSupplier) {
        return of("time", timeSupplier);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code {@link #DEFAULT_FORMAT}.
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
     * The format will be the message code {@link #DEFAULT_FORMAT}.
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
     * The format will be the message code {@link #DEFAULT_FORMAT}.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeSupplier the function to get the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull Component placeholder,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(ComponentUtils.toString(placeholder), timeSupplier);
    }

    /**
     * Creates a new Time argument.
     * The format will be the message code {@link #DEFAULT_FORMAT}.
     *
     * @param placeholder  the placeholder (without percentages)
     * @param timeSupplier the function to get the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return of(placeholder, DEFAULT_FORMAT, timeSupplier);
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
     * @param timeSupplier the function to get the time
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
     * @param timeSupplier the function to get the time
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
     * @param timeSupplier the function to get the time
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
     * @param timeSupplier the function to get the time
     * @return the time argument
     */
    public static @NotNull Time of(final @NotNull String placeholder,
                                   final @NotNull String timeFormat,
                                   final @NotNull Supplier<Long> timeSupplier) {
        return new Time(placeholder, timeFormat, timeSupplier);
    }

}
