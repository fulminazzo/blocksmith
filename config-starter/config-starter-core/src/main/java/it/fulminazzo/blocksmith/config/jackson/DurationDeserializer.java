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

final class DurationDeserializer extends StdDeserializer<Duration> {
    private static final int daysInMonth = 30;
    private static final int daysInYear = 365;

    private static final @NotNull Map<String, Function<Long, Duration>> parsers = new LinkedHashMap<>();

    private final @NotNull Logger logger;

    static {
        parsers.put("ns", Duration::ofNanos);
        parsers.put("ms", Duration::ofMillis);
        parsers.put("s", Duration::ofSeconds);
        parsers.put("m", Duration::ofMinutes);
        parsers.put("h", Duration::ofHours);
        parsers.put("d", Duration::ofDays);
        parsers.put("M", l -> Duration.ofDays(l * daysInMonth));
        parsers.put("y", l -> Duration.ofDays(l * daysInYear));
        parsers.put("Y", l -> Duration.ofDays(l * daysInYear));
    }

    public DurationDeserializer(final @NotNull Logger logger) {
        super(Duration.class);
        this.logger = logger;
    }

    @Override
    public Duration deserialize(final @NotNull JsonParser parser,
                                final @NotNull DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String raw = node.asText();
        Duration duration = null;

        for (String r : raw.split("[ \r\n\t]+")) {
            Map.@Nullable Entry<String, Function<Long, Duration>> durationParser = getParser(r);
            if (durationParser != null) {
                String unit = durationParser.getKey();
                String rawValue = r.substring(0, r.length() - unit.length());
                try {
                    long value = Long.parseLong(rawValue);
                    Duration d = durationParser.getValue().apply(value);
                    duration = duration == null ? d : duration.plus(d);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid time value '{}' for unit {} (path: {})", rawValue, unit, JacksonUtils.getCurrentPath(parser));
                }
            } else
                logger.warn("Unrecognized time notation '{}' (path: {})", r, JacksonUtils.getCurrentPath(parser));
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

    private static @Nullable Map.Entry<String, Function<Long, Duration>> getParser(final @NotNull String raw) {
        for (Map.Entry<String, Function<Long, Duration>> entry : parsers.entrySet())
            if (raw.endsWith(entry.getKey()))
                return entry;
        return null;
    }

    private static @NotNull String getSupportedUnits() {
        return String.join(", ", parsers.keySet());
    }

}
