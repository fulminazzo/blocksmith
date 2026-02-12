package it.fulminazzo.blocksmith.data.entity;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;
import org.joor.ReflectException;

import java.util.function.Function;

/**
 * Responsible for accessing information of an entity.
 *
 * @param <T>  the type of the entity
 * @param <ID> the type of the id of the entity (should be unique)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityMapper<T, ID> {
    private final @NotNull Class<T> entityType;
    private final @NotNull Function<T, ID> idMapper;

    /**
     * Gets the entity Java class.
     *
     * @return the type
     */
    public @NotNull Class<T> getType() {
        return entityType;
    }

    /**
     * Gets the ID of the entity.
     *
     * @param entity the entity
     * @return the id
     */
    public @NotNull ID getId(final @NotNull T entity) {
        return idMapper.apply(entity);
    }

    /**
     * Creates a new entity mapper.
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
            Reflect.onClass(type).field(idFieldName);
            return create(
                    type,
                    e -> Reflect.on(e).field(idFieldName).get()
            );
        } catch (ReflectException e) {
            throw new IllegalArgumentException(String.format("Could not find field '%s' in %s",
                    idFieldName, type.getCanonicalName())
            );
        }
    }

    /**
     * Creates a new entity mapper.
     *
     * @param <T>      the type of the entity
     * @param <ID>     the type of the id of the entity (should be unique)
     * @param type     the entity Java class
     * @param idMapper the function to get the ID of the entity
     * @return the entity mapper
     */
    public static <T, ID> @NotNull EntityMapper<T, ID> create(
            final @NotNull Class<T> type,
            final @NotNull Function<T, ID> idMapper
    ) {
        return new EntityMapper<>(type, idMapper);
    }

}
