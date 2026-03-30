package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.MapType;
import it.fulminazzo.blocksmith.config.Comment;
import it.fulminazzo.blocksmith.config.CommentUtils;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of utilities to work with jackson.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class JacksonUtils {

    /**
     * Sets up the given {@link ObjectMapper} so that many exceptions
     * (like missing property, extra property or invalid key/property type)
     * are replaced with a log warning.
     *
     * @param <M>                       the type of the mapper
     * @param mapper                    the mapper
     * @param logger                    the logger
     * @param commentPropertyWriterType the type of {@link CommentPropertyWriter} responsible for writing comments
     * @return the updated mapper
     */
    @SuppressWarnings("unchecked")
    public static <M extends ObjectMapper> M setupMapper(final @NotNull M mapper,
                                                         final @NotNull Logger logger,
                                                         final @Nullable Class<? extends CommentPropertyWriter> commentPropertyWriterType) {
        return (M) mapper
                .registerModule(new SimpleModule() {

                    @Override
                    public void setupModule(final @NotNull SetupContext context) {
                        super.setupModule(context);
                        context.addBeanDeserializerModifier(new JacksonBeanDeserializerModifier(logger));
                        if (commentPropertyWriterType != null)
                            context.addBeanSerializerModifier(new JacksonBeanSerializerModifier<>(commentPropertyWriterType));
                    }

                })
                .addHandler(new LoggerDeserializationProblemHandler(logger));
    }

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

    @RequiredArgsConstructor
    private static final class JacksonBeanDeserializerModifier extends BeanDeserializerModifier {
        private final @NotNull Logger logger;

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

    @RequiredArgsConstructor
    private static final class JacksonBeanSerializerModifier<W extends CommentPropertyWriter> extends BeanSerializerModifier {
        private final @NotNull Class<W> commentPropertyWriterType;

        @Override
        public List<BeanPropertyWriter> changeProperties(final SerializationConfig config,
                                                         final BeanDescription beanDescription,
                                                         final List<BeanPropertyWriter> beanProperties) {
            for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter beanProperty = beanProperties.get(i);
                Comment comment = beanProperty.getAnnotation(Comment.class);
                if (comment != null && !CommentUtils.isEmpty(comment)) {
                    W commentWriter = Reflect.on(commentPropertyWriterType)
                            .init(beanProperty, comment)
                            .get();
                    beanProperties.set(i, commentWriter);
                }
            }
            return beanProperties;
        }

    }

}
