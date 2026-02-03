package it.fulminazzo.config.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import it.fulminazzo.config.ConfigurationAdapter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.Iterator;

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
