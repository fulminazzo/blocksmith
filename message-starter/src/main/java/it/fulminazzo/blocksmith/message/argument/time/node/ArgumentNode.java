package it.fulminazzo.blocksmith.message.argument.time.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a time node where the time will actually be formatted.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ArgumentNode extends TimeNode {
    private final @NotNull String text;
    private final @NotNull TimeUnit timeUnit;
    private final @NotNull String singular;
    private final @NotNull String plural;

    private boolean optional;

    @Override
    protected @NotNull String parseSingle(final long time) {
        long actualTime = timeUnit.formatTime(time);
        if (optional && actualTime == 0) return "";
        return text
                .replace("%unit%", String.valueOf(actualTime))
                .replace("%name%", actualTime == 1 ? singular : plural);
    }

    /**
     * The supported time units for an Argument node.
     */
    @RequiredArgsConstructor
    public enum TimeUnit {
        MILLIS(1),
        SECONDS(1000),
        MINUTES(60 * SECONDS.timeInMillis),
        HOURS(60 * MINUTES.timeInMillis),
        DAYS(24 * HOURS.timeInMillis),
        WEEKS(7 * DAYS.timeInMillis),
        MONTHS(4 * WEEKS.timeInMillis),
        YEARS(12 * MONTHS.timeInMillis);

        private final long timeInMillis;

        /**
         * Gets the name that identifies this unit in the format.
         *
         * @return the name
         */
        public @NotNull String getName() {
            return name().toLowerCase();
        }

        /**
         * Formats the given time to the current unit.
         *
         * @param time the time (in milliseconds)
         * @return the formatted time
         */
        public long formatTime(final long time) {
            return time / timeInMillis;
        }

        /**
         * Attempts to get the corresponding unit from the given name.
         *
         * @param name the name
         * @return the time unit (if found)
         */
        public static @Nullable TimeUnit of(final @NotNull String name) {
            for (TimeUnit unit : TimeUnit.values())
                if (unit.getName().equals(name))
                    return unit;
            return null;
        }

    }

}
