package it.fulminazzo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A special implementation of {@link ConfigurationAdapter}
 * that uses the <a href="https://github.com/FasterXML/jackson">jackson project</a>
 * for serialization and deserialization.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class JacksonConfigurationAdapter implements ConfigurationAdapter {
    /**
     * Represents an error on deserialization of a key for a {@link java.util.Map}.
     */
    final static @NotNull Object INVALID_KEY_MARKER = new Object() {
        @Override
        public String toString() {
            return "INVALID_KEY_MARKER";
        }
    };

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
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    static class LoadProblemHandler extends DeserializationProblemHandler {
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
        public @NotNull Object handleWeirdKey(final @NotNull DeserializationContext context,
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
            return INVALID_KEY_MARKER;
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

    /**
     * A special {@link SimpleModule} to link {@link LenientMapDeserializer} for map deserialization.
     */
    static class LenientMapModule extends SimpleModule {

        @Override
        public void setupModule(final SetupContext context) {
            super.setupModule(context);

            context.addBeanDeserializerModifier(new BeanDeserializerModifier() {

                @Override
                public JsonDeserializer<?> modifyMapDeserializer(final DeserializationConfig config,
                                                                 final MapType type,
                                                                 final BeanDescription beanDesc,
                                                                 final JsonDeserializer<?> deserializer) {
                    if (deserializer instanceof MapDeserializer)
                        return new LenientMapDeserializer((MapDeserializer) deserializer);
                    else return deserializer;
                }

            });
        }

    }

    /**
     * A special type of {@link MapDeserializer} that will remove
     * any {@link #INVALID_KEY_MARKER} key from the deserialization result.
     */
    static class LenientMapDeserializer extends MapDeserializer {

        /**
         * Instantiates a new Lenient map deserializer.
         *
         * @param delegate the delegate deserializer
         */
        public LenientMapDeserializer(final @NotNull MapDeserializer delegate) {
            super(delegate);
        }

        @Override
        protected MapDeserializer withResolved(final KeyDeserializer keyDeserializer,
                                               final TypeDeserializer valueTypeDeserializer,
                                               final JsonDeserializer<?> valueDeserializer,
                                               final NullValueProvider nuller,
                                               final Set<String> ignorable,
                                               final Set<String> includable) {
            return new LenientMapDeserializer(
                    super.withResolved(keyDeserializer, valueTypeDeserializer, valueDeserializer, nuller, ignorable, includable)
            );
        }

        @Override
        public Map<Object, Object> deserialize(final JsonParser parser,
                                                         final DeserializationContext context) throws IOException {
            return cleanupMap(super.deserialize(parser, context));
        }

        @Override
        public Map<Object, Object> deserialize(final JsonParser parser,
                                                         final DeserializationContext context,
                                                         final Map<Object, Object> result) throws IOException {
            return cleanupMap(super.deserialize(parser, context, result));
        }

        private static @Nullable Map<Object, Object> cleanupMap(final @Nullable Map<Object, Object> map) {
            if (map != null) map.remove(INVALID_KEY_MARKER);
            return map;
        }

    }

}
