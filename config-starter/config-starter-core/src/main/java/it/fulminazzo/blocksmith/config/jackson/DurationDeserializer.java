package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A Jackson deserializer for {@link Duration} objects.
 */
final class DurationDeserializer extends StdDeserializer<Duration> {
    private static final long daysInMonth = 30;
    private static final long daysInYear = 365;
    private static final long millisInSecond = 1000;

    private static final @NotNull Map<String, Function<String, Duration>> parsers = new LinkedHashMap<>();

    private final @NotNull Logger logger;

    static {
        parsers.put("ns", s -> Duration.ofNanos(Long.parseLong(s)));
        parsers.put("ms", s -> Duration.ofMillis(Long.parseLong(s)));
        parsers.put("s", s -> {
            long secondsAndMillis = (long) (Double.parseDouble(s) * millisInSecond);
            return Duration.ofSeconds(secondsAndMillis / 1000).plusMillis(secondsAndMillis % 1000);
        });
        parsers.put("m", s -> Duration.ofMinutes(Long.parseLong(s)));
        parsers.put("h", s -> Duration.ofHours(Long.parseLong(s)));
        parsers.put("d", s -> Duration.ofDays(Long.parseLong(s)));
        parsers.put("M", s -> Duration.ofDays(Long.parseLong(s) * daysInMonth));
        parsers.put("y", s -> Duration.ofDays(Long.parseLong(s) * daysInYear));
        parsers.put("Y", s -> Duration.ofDays(Long.parseLong(s) * daysInYear));
    }

    /**
     * Instantiates a new Duration deserializer.
     *
     * @param logger the logger
     */
    public DurationDeserializer(final @NotNull Logger logger) {
        super(Duration.class);
        this.logger = logger;
    }

    @Override
    public Duration deserialize(final @NotNull JsonParser parser,
                                final @NotNull DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String raw = node.asText();
        try {
            return getParser("s").getValue().apply(raw);
        } catch (NumberFormatException ignored) {}
        Duration duration = null;

        for (String r : raw.split("[ \r\n\t]+")) {
            Map.@Nullable Entry<String, Function<String, Duration>> durationParser = getParser(r);
            if (durationParser != null) {
                String unit = durationParser.getKey();
                String rawValue = r.substring(0, r.length() - unit.length());
                try {
                    Duration d = durationParser.getValue().apply(rawValue);
                    duration = duration == null ? d : duration.plus(d);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid time value '{}' for unit {} (path: {})", rawValue, unit, JacksonUtils.getCurrentPath(parser));
                }
            } else
                logger.warn("Unrecognized time notation '{}'. Supported units: {} (path: {})", r, getSupportedUnits(), JacksonUtils.getCurrentPath(parser));
        }

        if (duration == null) {
            return (Duration) context.handleWeirdStringValue(
                    Duration.class,
                    raw,
                    ""
            );
        }
        return duration;
    }

    private static @Nullable Map.Entry<String, Function<String, Duration>> getParser(final @NotNull String raw) {
        for (Map.Entry<String, Function<String, Duration>> entry : parsers.entrySet())
            if (raw.endsWith(entry.getKey()))
                return entry;
        return null;
    }

    private static @NotNull String getSupportedUnits() {
        return String.join(", ", parsers.keySet());
    }

}
