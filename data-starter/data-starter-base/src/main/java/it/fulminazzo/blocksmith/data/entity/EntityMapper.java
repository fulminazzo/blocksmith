package it.fulminazzo.blocksmith.data.entity;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible for accessing information of an entity.
 *
 * @param <T>  the type of the entity
 * @param <ID> the type of the id of the entity (should be unique)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityMapper<T, ID> {
    private static final @NotNull String defaultIdFieldName = "id";

    @Getter
    private final @NotNull Class<T> type;
    @Getter
    private final @NotNull String idFieldName;
    private final @NotNull Function<T, ID> idMapper;

    /**
     * Gets the ID of the entity.
     *
     * @param entity the entity
     * @return the id
     */
    public @NotNull ID getId(final @NotNull T entity) {
        return Objects.requireNonNull(idMapper.apply(entity), "ID should not be null");
    }

    /**
     * Creates a new Entity mapper.
     * <br>
     * The ID lookup will follow the logic:
     * <ol>
     *     <li>attempts to find a field annotated with {@link Id} (only ONE field must be annotated);</li>
     *     <li>if it fails, attempts to find a field named {@link #defaultIdFieldName};</li>
     *     <li>if it fails, an exception is thrown notifying the user.</li>
     * </ol>
     *
     * @param <T>  the type of the entity
     * @param <ID> the type of the id of the entity (should be unique)
     * @param type the entity Java class
     * @return the entity mapper
     */
    public static <T, ID> @NotNull EntityMapper<T, ID> create(final @NotNull Class<T> type) {
        final Reflect reflect = Reflect.on(type);
        @NotNull List<Field> fields = reflect.getNonStaticFields().stream()
                .filter(f -> f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
        if (!fields.isEmpty()) {
            if (fields.size() > 1)
                throw new IllegalArgumentException(String.format(
                        "Invalid entity '%s'. Detected %s annotated fields with %s. Please choose only one field",
                        type.getCanonicalName(), fields.size(), Id.class.getSimpleName()
                ));
            return create(type, fields.get(0).getName());
        }
        try {
            reflect.getNonStaticField(defaultIdFieldName);
            return create(type, defaultIdFieldName);
        } catch (ReflectException e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid entity '%s'. Could not find field '%s' and no field annotated with %s was present",
                    type.getCanonicalName(), defaultIdFieldName, Id.class.getSimpleName()
            ));
        }
    }

    /**
     * Creates a new Entity mapper.
     *
     * @param <T>         the type of the entity
     * @param <ID>        the type of the id of the entity (should be unique)
     * @param type        the entity Java class
     * @param idFieldName the name of the field that identifies the entity
     * @return the entity mapper
     */
    public static <T, ID> @NotNull EntityMapper<T, ID> create(
            final @NotNull Class<T> type,
            final @NotNull String idFieldName
    ) {
        try {
            Reflect.on(type).getNonStaticField(idFieldName);
            return create(
                    type,
                    idFieldName,
                    e -> Reflect.on(e).getNonStatic(idFieldName).get()
            );
        } catch (ReflectException e) {
            throw new IllegalArgumentException(String.format("Could not find field '%s' in %s",
                    idFieldName, type.getCanonicalName())
            );
        }
    }

    /**
     * Creates a new Entity mapper.
     *
     * @param <T>         the type of the entity
     * @param <ID>        the type of the id of the entity (should be unique)
     * @param type        the entity Java class
     * @param idFieldName the name of the field that represents the ID of the entity
     * @param idMapper    the function to get the ID of the entity
     * @return the entity mapper
     */
    public static <T, ID> @NotNull EntityMapper<T, ID> create(
            final @NotNull Class<T> type,
            final @NotNull String idFieldName,
            final @NotNull Function<T, ID> idMapper
    ) {
        return new EntityMapper<>(type, idFieldName, idMapper);
    }

}
