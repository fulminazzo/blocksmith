package it.fulminazzo.blocksmith.config;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A builder to generate a Java bean from a configuration file.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BeanConfigurationBuilder {
    private static final @NotNull String defaultJavaPackage = "java.lang";

    @NotNull Map<CommentKey, Object> data;

    @NotNull Map<String, ImportDeclaration> imports;
    @NotNull Map<String, FieldDeclaration> fields = new HashMap<>();
    @NotNull Map<String, MethodDeclaration> methods = new HashMap<>();

    /**
     * Parses simple values such as <code>null</code>, primitives, wrappers,
     * {@link String}s or {@link java.util.Collection}s.
     * Generates {@link Comment} annotation and getter for the field.
     *
     * @param key   the key
     * @param value the value
     */
    void parseProperty(final @NotNull CommentKey key, final @Nullable Object value) {
        final String className = getTypeFromObject(value).getSimpleName();

        // field
        final FieldDeclaration field = fields.computeIfAbsent(
                key.getKey(),
                k -> new FieldDeclaration()
                        .setPrivate(true)
                        .setFinal(true)
                        .addVariable(new VariableDeclarator().setName(k))
        ).setAllTypes(StaticJavaParser.parseType(className));
        field.getVariable(0).setInitializer(getInitializer(value));

        // comment annotation
        convertComments(key, field);

        // getter
        methods.computeIfAbsent(
                "get" + capitalize(key.getKey()),
                k -> {
                    MethodDeclaration method = new MethodDeclaration()
                            .setPublic(true)
                            .setType(className);
                    method.createBody().addStatement(new ReturnStmt(new NameExpr(k)));
                    return method;
                }
        );
    }

    /**
     * Converts the comments of the key to a {@link Comment} annotation for the field.
     *
     * @param key   the key to pull the comments from
     * @param field the field to add the annotation to
     */
    void convertComments(final @NotNull CommentKey key, final @NotNull FieldDeclaration field) {
        final Expression initializer;
        final @NotNull List<String> comments = key.getComments();
        if (comments.isEmpty()) {
            field.getAnnotationByClass(Comment.class).ifPresent(Node::remove);
            return;
        } else if (comments.size() == 1) initializer = new StringLiteralExpr(comments.get(0));
        else
            initializer = new ArrayInitializerExpr(NodeList.nodeList(
                    comments.stream().map(StringLiteralExpr::new).collect(Collectors.toList())
            ));

        Optional<SingleMemberAnnotationExpr> annotation = field.getAnnotationByClass(Comment.class)
                .map(Expression::asSingleMemberAnnotationExpr);
        annotation.ifPresentOrElse(
                a -> a.setMemberValue(initializer),
                () -> field.addSingleMemberAnnotation(Comment.class, initializer)
        );
    }

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
     * If the import belongs to defaultJavaPackage,
     * then nothing is done (as already imported by default).
     *
     * @param value the value to get the type from (if <code>null</code>, nothing is done)
     */
    void addImport(final @Nullable Object value) {
        addImport(getTypeFromObject(value));
    }

    /**
     * Adds an import to the imports list.
     * If the import belongs to defaultJavaPackage,
     * then nothing is done (as already imported by default).
     *
     * @param type the type to add
     */
    void addImport(final @NotNull Class<?> type) {
        addImport(type.getCanonicalName());
    }

    /**
     * Adds an import to the imports list.
     * If the import belongs to {@link #defaultJavaPackage},
     * then nothing is done (as already imported by default).
     *
     * @param classCanonicalName the canonical name of the class to add
     */
    void addImport(final @NotNull String classCanonicalName) {
        if (!classCanonicalName.startsWith(defaultJavaPackage))
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

    private static @NotNull String capitalize(final @NotNull String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
