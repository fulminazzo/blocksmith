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
    private static final @NotNull String genericsFormat = "<%s>";
    private static final @NotNull Class<?> nullClass = Object.class;

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
        final String className = getGenericTypeNameFromObject(value);

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
                            .setType(className)
                            .setName(k);
                    method.createBody().addStatement(new ReturnStmt(new NameExpr(key.getKey())));
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
     * Gets the type name of the given object.
     * If the object is a collection, the generic type is returned.
     *
     * @param object the object
     * @return the type name
     */
    @NotNull String getGenericTypeNameFromObject(final @Nullable Object object) {
        String typeName = getTypeFromObject(object).getSimpleName();
        if (object instanceof Collection<?>)
            typeName += String.format(genericsFormat, guessCollectionGenericType((Collection<?>) object));
        return typeName;
    }

    /**
     * Converts the given value to a class.
     * If the value is <code>null</code>, {@link Object} is returned.
     *
     * @param value the value
     * @return the class
     */
    static @NotNull Class<?> getTypeFromObject(final @Nullable Object value) {
        return value == null ? nullClass : value.getClass();
    }

    private static @NotNull String capitalize(final @NotNull String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Attempts to guess the generic type of the given collection based on its elements types.
     *
     * @param collection the collection
     * @return the guessed type name (with its generic parameters if present)
     */
    private static @NotNull String guessCollectionGenericType(final @NotNull Collection<?> collection) {
        final Map<String, Integer> typesCount = new LinkedHashMap<>();
        for (Object object : collection) {
            final Set<String> typeNames;
            if (object instanceof Collection<?>) {
                String genericType = guessCollectionGenericType((Collection<?>) object);
                typeNames = getCollectionTypeNames(object.getClass(), genericType);
            } else if (object == null) typeNames = Set.of(nullClass.getCanonicalName());
            else typeNames = getBasicTypeNames(object.getClass(), object.getClass());
            typeNames.forEach(t -> typesCount.merge(t, 1, Integer::sum));
        }
        return typesCount.entrySet().stream()
                .filter(e -> e.getValue() == collection.size())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(nullClass.getCanonicalName());
    }

    /**
     * Gets all the names of the classes (given, inherited and interface) of the basic type.
     * A basic type is a primitive, a wrapper, {@link String} or {@link Object}.
     * If the class requires a generic type, the base one is provided.
     *
     * @param type the class to get the types of
     * @param base the type to get the types of
     * @return the types names
     */
    private static @NotNull Set<String> getBasicTypeNames(final @Nullable Class<?> type,
                                                          final @NotNull Class<?> base) {
        final Set<String> typeNames = new LinkedHashSet<>();
        if (type == null) return typeNames;
        String typeName = type.getCanonicalName();
        if (type.getTypeParameters().length == 1) typeName += String.format(genericsFormat, base.getSimpleName());
        typeNames.add(typeName);
        if (type.equals(Object.class)) return typeNames;
        typeNames.addAll(getBasicTypeNames(type.getSuperclass(), base));
        for (Class<?> interfaceType : type.getInterfaces())
            typeNames.addAll(getBasicTypeNames(interfaceType, base));
        return typeNames;
    }

    /**
     * Gets all the names of the classes (given, inherited and interfaces) of the given collection type.
     * If the class requires a generic type, the given one is provided.
     *
     * @param type        the class to get the types of
     * @param genericType the generic type
     * @return the types names
     */
    private static @NotNull Set<String> getCollectionTypeNames(final @Nullable Class<?> type,
                                                               final @NotNull String genericType) {
        final Set<String> typeNames = new LinkedHashSet<>();
        if (type == null || type.equals(Object.class)) return typeNames;
        String typeName = type.getCanonicalName();
        if (type.getTypeParameters().length == 1) typeName += String.format(genericsFormat, genericType);
        typeNames.add(typeName);
        typeNames.addAll(getCollectionTypeNames(type.getSuperclass(), genericType));
        for (Class<?> interfaceType : type.getInterfaces())
            typeNames.addAll(getCollectionTypeNames(interfaceType, genericType));
        return typeNames;
    }

}
