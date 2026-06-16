package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig;
import it.fulminazzo.blocksmith.broker.config.MessageBrokerType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Locale;

/**
 * Jackson deserializer for {@link MessageBrokerConfig} objects.
 *
 * @see MessageBrokerConfig
 */
final class MessageBrokerConfigDeserializer extends StdDeserializer<MessageBrokerConfig<?>> {
    private static final long serialVersionUID = -8895833223541659868L;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private final transient @NotNull Logger logger;

    /**
     * Instantiates a new Data source config deserializer.
     *
     * @param logger the logger
     */
    public MessageBrokerConfigDeserializer(final @NotNull Logger logger) {
        super(MessageBrokerConfig.class);
        this.logger = logger;
    }

    @Override
    public MessageBrokerConfig<?> deserialize(
            final @NotNull JsonParser jsonParser,
            final @NotNull DeserializationContext deserializationContext
    ) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        JsonNode typeNode = node.get("type");
        final String rawType = typeNode == null ? "null" : typeNode.asText();
        try {
            MessageBrokerType type = MessageBrokerType.valueOf(rawType.toUpperCase(Locale.ROOT));
            ((ObjectNode) node).remove("type");
            return deserializationContext.readTreeAsValue(node, type.getConfigClass());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid message broker configuration: unrecognized type '{}'", rawType);
            throw new IOException(
                    String.format("Invalid message broker configuration: unrecognized type '%s'", rawType)
            );
        }
    }

}
