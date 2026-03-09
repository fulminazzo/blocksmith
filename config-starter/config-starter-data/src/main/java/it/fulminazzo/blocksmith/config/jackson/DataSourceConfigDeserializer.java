package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceType;

import java.io.IOException;

final class DataSourceConfigDeserializer extends StdDeserializer<DataSourceConfig> {

    public DataSourceConfigDeserializer() {
        super(DataSourceConfig.class);
    }

    @Override
    public DataSourceConfig deserialize(final JsonParser jsonParser,
                                        final DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String rawType = node.get("type").asText();
        try {
            if (rawType == null) throw new IllegalArgumentException();
            DataSourceType type = DataSourceType.valueOf(rawType.toUpperCase());
            return type.newConfig();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid datasource configuration type: " + rawType);
        }
    }


}
