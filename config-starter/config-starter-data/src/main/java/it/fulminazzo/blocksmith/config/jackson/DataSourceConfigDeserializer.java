package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Locale;

/**
 * Jackson deserializer for {@link DataSourceConfig} objects.
 *
 * @see DataSourceConfig
 */
final class DataSourceConfigDeserializer extends StdDeserializer<DataSourceConfig> {
    private static final long serialVersionUID = -1724048481514521412L;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private final transient @NotNull Logger logger;

    /**
     * Instantiates a new Data source config deserializer.
     *
     * @param logger the logger
     */
    public DataSourceConfigDeserializer(final @NotNull Logger logger) {
        super(DataSourceConfig.class);
        this.logger = logger;
    }

    @Override
    public DataSourceConfig deserialize(
            final @NotNull JsonParser jsonParser,
            final @NotNull DeserializationContext deserializationContext
    ) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String rawType = node.get("type").asText();
        try {
            if (rawType == null) throw new IllegalArgumentException();
            DataSourceType type = DataSourceType.valueOf(rawType.toUpperCase(Locale.ROOT));
            ((ObjectNode) node).remove("type");
            return deserializationContext.readTreeAsValue(node, type.getConfigClass());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid database configuration: unidentified type '{}'", rawType);
            return null;
        }
    }

}
