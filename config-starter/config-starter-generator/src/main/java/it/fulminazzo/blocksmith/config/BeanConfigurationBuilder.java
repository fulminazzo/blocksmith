package it.fulminazzo.blocksmith.config;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A builder to generate a Java bean from a configuration file.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BeanConfigurationBuilder {
    @NotNull Map<CommentKey, Object> data;

    @NotNull Map<String, ImportDeclaration> imports;
    @NotNull Map<String, FieldDeclaration> fields = new HashMap<>();
    @NotNull Map<String, MethodDeclaration> methods = new HashMap<>();

    /**
     * Gets the initializer value for the given value.
     * <br>
     * For example, for strings the initializer is <code>"value"</code>.
     * <br>
     * If collections are given, they are imported and initialized properly.
     *
     * @param value the value
     * @return the initializer value
     */
    @NotNull String getInitializer(final @Nullable Object value) {
        if (value == null) return "null";
        else if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            addImport(collection);
            addImport(Arrays.class);
            return String.format("new %s<>(Arrays.asList(%s))",
                    collection.getClass().getSimpleName(),
                    collection.stream().map(this::getInitializer).collect(Collectors.joining(", "))
            );
        } else if (value instanceof String) return "\"" + value + "\"";
        else if (value instanceof Character) return "'" + value + "'";
        else return value.toString();
    }

    /**
     * Adds an import to the imports list.
     * If the import belongs to "java.lang",
     * then nothing is done (as already imported by default).
     *
     * @param value the value to get the type from (if <code>null</code>, nothing is done)
     */
    void addImport(final @Nullable Object value) {
        addImport(getTypeFromObject(value));
    }

    /**
     * Adds an import to the imports list.
     * If the import belongs to "java.lang",
     * then nothing is done (as already imported by default).
     *
     * @param type the type to add
     */
    void addImport(final @NotNull Class<?> type) {
        addImport(type.getCanonicalName());
    }

    /**
     * Adds an import to the imports list.
     * If the import belongs to "java.lang",
     * then nothing is done (as already imported by default).
     *
     * @param classCanonicalName the canonical name of the class to add
     */
    void addImport(final @NotNull String classCanonicalName) {
        if (!classCanonicalName.startsWith("java.lang"))
            imports.computeIfAbsent(
                    classCanonicalName,
                    c -> new ImportDeclaration(c, false, false)
            );
    }

    /**
     * Converts the given value to a class.
     * If the value is <code>null</code>, {@link Object} is returned.
     *
     * @param value the value
     * @return the class
     */
    static @NotNull Class<?> getTypeFromObject(final @Nullable Object value) {
        return value == null ? Object.class : value.getClass();
    }

}
