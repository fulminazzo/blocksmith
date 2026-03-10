package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A special implementation of {@link DeserializationProblemHandler} to
 * catch deserialization errors and send warning messages through a logger.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class LoggerDeserializationProblemHandler extends DeserializationProblemHandler {
    @Nullable Logger logger;

    @Override
    public boolean handleUnknownProperty(final @Nullable DeserializationContext context,
                                         final @NotNull JsonParser parser,
                                         final @Nullable JsonDeserializer<?> deserializer,
                                         final @Nullable Object beanOrClass,
                                         final @NotNull String propertyName) throws IOException {
        // when the JSON contains a property not present in the bean
        String path = JacksonUtils.getCurrentPath(parser);
        if (logger != null) logger.warn("Ignoring unrecognized property '{}' (path: '{}')", propertyName, path);
        parser.skipChildren();
        return true;
    }

    @Override
    public @Nullable Object handleWeirdKey(final @NotNull DeserializationContext context,
                                           final @NotNull Class<?> rawKeyType,
                                           final @NotNull String keyValue,
                                           final @Nullable String failureMsg) {
        // when the key of a Map cannot be converted to the expected type (e.g. Integer)
        String path = JacksonUtils.getCurrentPath(context.getParser());
        if (logger != null) logger.warn("Invalid key '{}' for map: expected {} (path: '{}')",
                keyValue,
                rawKeyType.getCanonicalName(),
                path
        );
        return null;
    }

    @Override
    public Object handleWeirdStringValue(final DeserializationContext context,
                                         final Class<?> targetType,
                                         final String valueToConvert,
                                         final String failureMsg) {
        // when a string cannot be converted to the requested type (e.g. LocalDate)
        throw new LoggerSettableBeanProperty.DeserializationException(
                "Invalid value for property '<name>': expected <type> but got '%s' (path: '%s')",
                valueToConvert, JacksonUtils.getCurrentPath(context.getParser())
        );
    }

    @Override
    public Object handleWeirdNumberValue(final DeserializationContext context,
                                         final Class<?> targetType,
                                         final Number valueToConvert,
                                         final String failureMsg) {
        // when a number cannot be converted to the requested type (e.g. too big)
        throw new LoggerSettableBeanProperty.DeserializationException(
                "Invalid value for property '<name>': expected <type> but got '%s' (path: '%s')",
                valueToConvert, JacksonUtils.getCurrentPath(context.getParser())
        );
    }

    @Override
    public Object handleUnexpectedToken(final DeserializationContext context,
                                        final JavaType targetType,
                                        final JsonToken token,
                                        final JsonParser parser,
                                        final String failureMsg) {
        // when the value is different from the expected type
        throw new LoggerSettableBeanProperty.DeserializationException(
                "Invalid value for property '<name>': expected <type> but got token '%s' (path: '%s')",
                token, JacksonUtils.getCurrentPath(parser)
        );
    }

}
