package it.fulminazzo.blocksmith.config.nightconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import it.fulminazzo.blocksmith.config.Comment;
import it.fulminazzo.blocksmith.config.CommentUtils;
import it.fulminazzo.blocksmith.naming.CaseConverter;
import it.fulminazzo.blocksmith.naming.Convention;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 * A collection of utilities to work with {@link Config} objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NightConfigUtils {
    private static final @NotNull Convention namingConvention = Convention.SNAKE_CASE;

    /**
     * Updates all the properties names of the given configuration
     * to match the TOML default naming strategy.
     *
     * @param configuration the configuration
     */
    public static void fixPropertyNames(final @NotNull Config configuration) {
        for (Config.Entry entry : new HashSet<>(configuration.entrySet())) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String translation = CaseConverter.convert(key, namingConvention);
            if (!key.equals(translation)) {
                configuration.remove(key);
                configuration.set(translation, value);
            }
            if (value instanceof Config) fixPropertyNames((Config) value);
        }
    }

    /**
     * Loops all the given object fields in search for the {@link Comment} annotation.
     * If present, will set the comments to the corresponding path.
     *
     * @param reference     the reference object
     * @param configuration the configuration
     */
    public static void setComments(final @NotNull Object reference,
                                   final @NotNull CommentedConfig configuration) {
        Reflect reflect = Reflect.on(reference);
        for (Field field : reflect.getFields(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()))) {
            String propertyName = CaseConverter.convert(field.getName(), namingConvention);
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                configuration.setComment(propertyName, getCommentValue(comment));
            }
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive() || fieldType.equals(Object.class)) continue;
            Object fieldValue = reflect.get(field).get();
            if (fieldValue == null || fieldValue.equals(reference)) continue;
            Object configValue = configuration.get(propertyName);
            if (configValue instanceof CommentedConfig)
                setComments(fieldValue, (CommentedConfig) configValue);
        }
    }

    /**
     * Gets the comment value, indented for each line.
     *
     * @param comment the comment
     * @return the comment value ({@code null} if empty or only white spaces)
     */
    static @Nullable String getCommentValue(final @NotNull Comment comment) {
        String commentText = String.join("\n", CommentUtils.getText(comment));
        if (commentText.trim().isEmpty()) return null;
        return commentText.replaceAll("^|\n", "\n ").substring(1);
    }

}
