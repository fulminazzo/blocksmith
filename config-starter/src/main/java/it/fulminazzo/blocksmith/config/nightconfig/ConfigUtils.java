package it.fulminazzo.blocksmith.config.nightconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import it.fulminazzo.blocksmith.config.Comment;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * A collection of utilities to work with {@link Config} objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtils {

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
            String fieldName = field.getName();
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                configuration.setComment(fieldName, comment.value());
            }
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive() || fieldType.equals(Object.class)) continue;
            Object fieldValue = ReflectionUtils.getFieldValue(reference, field);
            if (fieldValue == null || fieldValue.equals(reference)) continue;
            Object configValue = configuration.get(fieldName);
            if (configValue instanceof CommentedConfig)
                setComments(fieldValue, (CommentedConfig) configValue);
        }
    }

}
