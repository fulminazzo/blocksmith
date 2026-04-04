package it.fulminazzo.blocksmith.config;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
