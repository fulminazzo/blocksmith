package it.fulminazzo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * A special implementation of {@link ConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class JacksonConfigurationAdapter implements ConfigurationAdapter {
    @NotNull ObjectMapper mapper;

    @Override
    public @NotNull <T> T load(final @NotNull File file, final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void store(final @NotNull T configuration, final @NotNull File file) {
        throw new UnsupportedOperationException();
    }

    /**
     * An implementation of {@link DeserializationProblemHandler} to
     * catch deserialization errors and send warning messages.
     */
    static class LoadProblemHandler extends DeserializationProblemHandler {

        @Override
        public boolean handleUnknownProperty(final DeserializationContext context,
                                             final JsonParser parser,
                                             final JsonDeserializer<?> deserializer,
                                             final Object beanOrClass,
                                             final String propertyName) throws IOException {
            // when the JSON contains a property not present in the bean
            return super.handleUnknownProperty(context, parser, deserializer, beanOrClass, propertyName);
        }

        @Override
        public Object handleWeirdKey(final DeserializationContext context,
                                     final Class<?> rawKeyType,
                                     final String keyValue,
                                     final String failureMsg) throws IOException {
            // when the key of a Map cannot be converted to the expected type (e.g. Integer)
            return super.handleWeirdKey(context, rawKeyType, keyValue, failureMsg);
        }

        @Override
        public Object handleWeirdStringValue(final DeserializationContext context,
                                             final Class<?> targetType,
                                             final String valueToConvert,
                                             final String failureMsg) throws IOException {
            // when a string cannot be converted to the requested type (e.g. LocalDate)
            return super.handleWeirdStringValue(context, targetType, valueToConvert, failureMsg);
        }

        @Override
        public Object handleWeirdNumberValue(final DeserializationContext context,
                                             final Class<?> targetType,
                                             final Number valueToConvert,
                                             final String failureMsg) throws IOException {
            // when a number cannot be converted to the requested type (e.g. too big)
            return super.handleWeirdNumberValue(context, targetType, valueToConvert, failureMsg);
        }

        @Override
        public Object handleUnexpectedToken(final DeserializationContext context,
                                            final JavaType targetType,
                                            final JsonToken token,
                                            final JsonParser parser,
                                            final String failureMsg) throws IOException {
            // when the value is different from the expected type
            return super.handleUnexpectedToken(context, targetType, token, parser, failureMsg);
        }

    }

}
