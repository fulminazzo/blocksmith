package it.fulminazzo.blocksmith.config.nightconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import it.fulminazzo.blocksmith.config.Comment;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

/**
 * A collection of utilities to work with {@link Config} objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtils {

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
            String translation = formatToSnakeCase(key);
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
        Collection<Field> fields = ReflectionUtils.getInstanceFields(reference.getClass());
        for (Field field : fields) {
            String propertyName = formatToSnakeCase(field.getName());
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                configuration.setComment(propertyName, getCommentValue(comment));
            }
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive() || fieldType.equals(Object.class)) continue;
            Object fieldValue = ReflectionUtils.getFieldValue(reference, field);
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
     * @return the comment value (<code>null</code> if empty or only white spaces)
     */
    static @Nullable String getCommentValue(final @NotNull Comment comment) {
        String commentText = comment.value();
        if (commentText.trim().isEmpty()) return null;
        return commentText.replaceAll("^|\n", "\n ").substring(1);
    }

    /**
     * Formats the given string to a snake case.
     *
     * @param string the string
     * @return the snake case string
     */
    public static String formatToSnakeCase(String string) {
        if (string == null) return null;
        String result = string.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        result = result.replaceAll("[\\s\\-.]", "_");
        return result.toLowerCase().replaceAll("_{2,}", "_");
    }

}
