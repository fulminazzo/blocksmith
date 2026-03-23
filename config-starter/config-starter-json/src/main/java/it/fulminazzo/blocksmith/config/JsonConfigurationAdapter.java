package it.fulminazzo.blocksmith.config;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.fulminazzo.blocksmith.config.jackson.JacksonConfigurationAdapter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Implementation of {@link BaseConfigurationAdapter} for JSON.
 */
final class JsonConfigurationAdapter implements BaseConfigurationAdapter {
    @Delegate
    private final @NotNull BaseConfigurationAdapter delegate;

    /**
     * Instantiates a new JSON configuration adapter.
     *
     * @param logger the logger
     */
    public JsonConfigurationAdapter(final @Nullable Logger logger) {
        this.delegate = new JacksonConfigurationAdapter(
                new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .setDefaultPrettyPrinter(new JsonPrettyPrinter()),
                logger,
                null
        );
    }

    /**
     * A special {@link DefaultPrettyPrinter} that overrides {@link #_objectFieldValueSeparatorWithSpaces}.
     */
    static final class JsonPrettyPrinter extends DefaultPrettyPrinter {

        /**
         * Instantiates a new JSON pretty printer.
         */
        public JsonPrettyPrinter() {
            _objectFieldValueSeparatorWithSpaces = ": ";
            indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new JsonPrettyPrinter();
        }

    }

}
