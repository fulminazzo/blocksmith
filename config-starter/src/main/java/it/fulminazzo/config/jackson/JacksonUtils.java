package it.fulminazzo.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A collection of utilities to work with jackson.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class JacksonUtils {

    /**
     * Given the parser, returns the path of the current context in a <b>dot notation</b>.
     *
     * @param parser the parser
     * @return the current path
     */
    public static @NotNull String getCurrentPath(final @NotNull JsonParser parser) {
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

    /**
     * A special {@link SimpleModule} to apply all the rules preventing
     * throwing exceptions on errors.
     */
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    static final class JacksonConfigurationModule extends SimpleModule {
        @NotNull Logger logger;

        @Override
        public void setupModule(final SetupContext context) {
            super.setupModule(context);
            context.addBeanDeserializerModifier(new JacksonConfigurationBeanDeserializerModifier(logger));
        }

    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class JacksonConfigurationBeanDeserializerModifier extends BeanDeserializerModifier {
        @NotNull Logger logger;

        @Override
        public BeanDeserializerBuilder updateBuilder(final DeserializationConfig config,
                                                     final BeanDescription beanDescription,
                                                     final BeanDeserializerBuilder builder) {
            Iterator<SettableBeanProperty> it = builder.getProperties();
            while (it.hasNext()) {
                SettableBeanProperty property = it.next();
                beanDescription.findProperties().stream()
                        .filter(p -> p.getName().equals(property.getName()))
                        .findFirst().ifPresent(description -> {
                            LoggerSettableBeanProperty beanProperty = new LoggerSettableBeanProperty(
                                    property,
                                    logger,
                                    description.getField()
                            );
                            builder.addOrReplaceProperty(beanProperty, true);
                        });
            }
            return builder;
        }

        @Override
        public JsonDeserializer<?> modifyMapDeserializer(final DeserializationConfig config,
                                                         final MapType type,
                                                         final BeanDescription beanDesc,
                                                         final JsonDeserializer<?> deserializer) {
            if (deserializer instanceof MapDeserializer)
                return new NonNullKeyMapDeserializer((MapDeserializer) deserializer);
            else return deserializer;
        }

    }

}
