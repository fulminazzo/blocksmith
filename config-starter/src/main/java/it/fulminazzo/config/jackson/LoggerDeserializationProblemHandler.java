package it.fulminazzo.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
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
import java.util.LinkedList;

/**
 * A special implementation of {@link DeserializationProblemHandler} to
 * catch deserialization errors and send warning messages through a logger.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class LoggerDeserializationProblemHandler extends DeserializationProblemHandler {
    @NotNull Logger logger;

    @Override
    public boolean handleUnknownProperty(final @Nullable DeserializationContext context,
                                         final @NotNull JsonParser parser,
                                         final @Nullable JsonDeserializer<?> deserializer,
                                         final @Nullable Object beanOrClass,
                                         final @NotNull String propertyName) throws IOException {
        // when the JSON contains a property not present in the bean
        String path = getCurrentPath(parser);
        logger.warn("Ignoring unrecognized property '{}' (path: '{}')", propertyName, path);
        parser.skipChildren();
        return true;
    }

    @Override
    public @Nullable Object handleWeirdKey(final @NotNull DeserializationContext context,
                                           final @NotNull Class<?> rawKeyType,
                                           final @NotNull String keyValue,
                                           final @Nullable String failureMsg) {
        // when the key of a Map cannot be converted to the expected type (e.g. Integer)
        String path = getCurrentPath(context.getParser());
        logger.warn("Invalid key '{}' for map: expected {} (path: '{}')",
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

    /**
     * Given the parser, returns the path of the current context in a <b>dot notation</b>.
     *
     * @param parser the parser
     * @return the current path
     */
    static @NotNull String getCurrentPath(final @NotNull JsonParser parser) {
        LinkedList<String> path = new LinkedList<>();

        JsonStreamContext context = parser.getParsingContext();
        while (context != null) {
            if (context.inArray()) path.addFirst(String.format("[%s]", context.getCurrentIndex()));
            else {
                String currentName = context.getCurrentName();
                if (currentName != null) path.addFirst("." + currentName);
            }
            context = context.getParent();
        }

        String finalPath = String.join("", path);
        if (!finalPath.isEmpty()) finalPath = finalPath.substring(1);
        return finalPath;
    }

}
