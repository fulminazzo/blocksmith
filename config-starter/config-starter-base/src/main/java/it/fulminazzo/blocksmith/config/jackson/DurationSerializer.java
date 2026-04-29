package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;

/**
 * A Jackson serializer for {@link Duration} objects.
 */
final class DurationSerializer extends StdSerializer<Duration> {
    private static final int daysInYear = 365;
    private static final int daysInMonth = 30;
    private static final int secondsInDay = 86400;
    private static final int secondsInHour = 3600;
    private static final int secondsInMinute = 60;
    private static final long nanosInMillis = 1_000_000;

    /**
     * Instantiates a new Duration serializer.
     */
    public DurationSerializer() {
        super(Duration.class);
    }

    @Override
    public void serialize(final @NotNull Duration value,
                          final @NotNull JsonGenerator gen,
                          final @NotNull SerializerProvider provider) throws IOException {
        Duration abs = value.abs();
        final long totalSeconds = abs.getSeconds();
        final int nanosOfSecond = abs.getNano();

        long totalDays = totalSeconds / secondsInDay;

        long years = totalDays / daysInYear;
        long daysRemainder = totalDays % daysInYear;

        long months = daysRemainder / daysInMonth;
        long days = daysRemainder % daysInMonth;

        long secondsRemainder = totalSeconds % secondsInDay;
        long hours = secondsRemainder / secondsInHour;
        secondsRemainder %= secondsInHour;

        long minutes = secondsRemainder / secondsInMinute;
        long seconds = secondsRemainder % secondsInMinute;

        long millis = nanosOfSecond / nanosInMillis;
        long nanos = nanosOfSecond % nanosInMillis;

        if (years == 0 && months == 0 && days == 0 && hours == 0 && minutes == 0 && nanos == 0 && (seconds != 0 || millis != 0)) {
            gen.writeNumber((value.isNegative() ? "-" : "") + formatSeconds(seconds, millis));
            return;
        }

        final StringBuilder builder = new StringBuilder();

        if (years != 0) builder.append(value.isNegative() ? "-" : "").append(years).append("y");
        if (months != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(months).append("M");
        if (days != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(days).append("d");
        if (hours != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(hours).append("h");
        if (minutes != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(minutes).append("m");
        if (seconds != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(seconds).append("s");
        if (millis != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(millis).append("ms");
        if (nanos != 0)
            builder.append(builder.length() > 0 ? " " : "").append(value.isNegative() ? "-" : "").append(nanos).append("ns");

        if (builder.length() == 0) gen.writeNumber(0);
        else gen.writeString(builder.toString());
    }

    private static @NotNull String formatSeconds(final long integerPart, long decimalPart) {
        if (decimalPart == 0) return String.valueOf(integerPart);
        String decStr = String.format("%03d", decimalPart);
        decStr = decStr.replaceAll("0+$", "");
        return integerPart + "." + decStr;
    }

}
