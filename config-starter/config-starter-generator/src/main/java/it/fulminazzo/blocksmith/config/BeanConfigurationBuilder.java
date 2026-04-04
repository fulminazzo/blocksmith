package it.fulminazzo.blocksmith.config;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import it.fulminazzo.blocksmith.structure.Pair;
import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A builder to generate a Java bean from a configuration file.
 */
@Slf4j
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BeanConfigurationBuilder {
    private static final @NotNull String defaultJavaPackage = "java.lang";
    private static final @NotNull String genericsFormat = "<%s>";
    private static final @NotNull Class<?> nullClass = Object.class;

    @NotNull Map<CommentKey, Object> data;

    @NotNull ClassOrInterfaceDeclaration root;
    @NotNull Map<String, ImportDeclaration> imports;
    @NotNull Map<String, ClassOrInterfaceDeclaration> nestedClasses = new HashMap<>();
    @NotNull Map<String, FieldDeclaration> fields = new HashMap<>();
    @NotNull Map<String, MethodDeclaration> methods = new HashMap<>();

    /**
     * Instantiates a new Bean configuration builder.
     *
     * @param data    the data
     * @param root    the root
     * @param imports the imports
     */
    BeanConfigurationBuilder(final @NotNull Map<CommentKey, Object> data,
                             final @NotNull ClassOrInterfaceDeclaration root,
                             final @NotNull Map<String, ImportDeclaration> imports) {
        this.data = data;
        this.root = root;
        this.imports = imports;

        root.getMembers().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(ClassOrInterfaceDeclaration.class::cast)
                .filter(d -> !d.isInterface())
                .forEach(d -> nestedClasses.put(d.getNameAsString(), d));

        root.getFields().forEach(f -> f.getVariables().forEach(
                v -> fields.put(v.getNameAsString(), f)
        ));

        root.getMethods().forEach(m -> methods.put(m.getNameAsString(), m));
    }

    /**
     * Converts all the given {@link #data} to Java fields for the bean creation.
     */
    public void parse() {
        data.forEach((k, v) -> {
            if (v instanceof Map<?, ?>) parseNestedConfig(k, (Map<CommentKey, Object>) v);
            else parseProperty(k, v);
        });
    }

    /**
     * Parses nested values in the form of {@link Map}s.
     * Generates {@link Comment} annotation, getter and setter for the field.
     *
     * @param key  the key
     * @param data the data
     */
    void parseNestedConfig(final @NotNull CommentKey key, final @NotNull Map<CommentKey, Object> data) {
        final String propertyName = key.getKey();
        final String nestedClassName = capitalize(propertyName);

        generateField(key, nestedClassName).setInitializer(
                new ObjectCreationExpr().setType(new ClassOrInterfaceType(null, nestedClassName))
        );

        // nested class
        ClassOrInterfaceDeclaration nestedClass = nestedClasses.computeIfAbsent(
                nestedClassName,
                k -> {
                    ClassOrInterfaceDeclaration nc = new ClassOrInterfaceDeclaration()
                            .setName(k)
                            .setPublic(true)
                            .setStatic(true);
                    root.addMember(nc);
                    return nc;
                });
        new BeanConfigurationBuilder(data, nestedClass, imports).parse();
    }

    /**
     * Parses simple values such as <code>null</code>, primitives, wrappers,
     * {@link String}s or {@link java.util.Collection}s.
     * Generates {@link Comment} annotation, getter and setter for the field.
     *
     * @param key   the key
     * @param value the value
     */
    void parseProperty(final @NotNull CommentKey key, final @Nullable Object value) {
        final String className = getGenericTypeNameFromObject(value);
        generateField(key, className).setInitializer(getInitializer(value));
    }

    private @NotNull VariableDeclarator generateField(final @NotNull CommentKey key,
                                                      final @NotNull String fieldClassName) {
        final String propertyName = key.getKey();
        final Type type = StaticJavaParser.parseType(fieldClassName);

        // field
        final FieldDeclaration field = fields.computeIfAbsent(
                propertyName,
                k -> root.addPrivateField(type, propertyName)
        );
        if (field.getVariables().isEmpty()) field.addVariable(new VariableDeclarator().setName(propertyName));

        // comment annotation
        convertComments(key, field);

        // getter
        methods.computeIfAbsent(
                "get" + capitalize(propertyName),
                k -> {
                    MethodDeclaration method = root.addMethod(k).setPublic(true);
                    method.createBody().addStatement(new ReturnStmt(new NameExpr(propertyName)));
                    return method;
                }
        ).setType(fieldClassName).setAbstract(false);

        // setter
        MethodDeclaration setter = methods.computeIfAbsent(
                "set" + capitalize(propertyName),
                k -> {
                    MethodDeclaration method = root.addMethod(k).setPublic(true);
                    method.createBody().addStatement(new AssignExpr(
                            new FieldAccessExpr(new ThisExpr(), propertyName),
                            new NameExpr(propertyName),
                            AssignExpr.Operator.ASSIGN
                    ));
                    return method;
                }
        ).setType("void").setAbstract(false);
        if (setter.getParameters().isEmpty()) setter.addParameter(type, propertyName);
        setter.getParameter(0).setType(type).setName(propertyName).setFinal(true);

        return field.getVariable(0).setType(type);
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
        if (object instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) object;
            if (collection instanceof List) typeName = List.class.getSimpleName();
            else if (collection instanceof Set) typeName = Set.class.getSimpleName();
            typeName += String.format(genericsFormat, guessCollectionGenericType(collection));
            return parseGenericTypesImports(typeName);
        } else return typeName;
    }

    private @NotNull String parseGenericTypesImports(final @NotNull String genericType) {
        addImport(genericType);
        int index = genericType.indexOf('<');
        if (index == -1) return genericType.substring(genericType.lastIndexOf('.') + 1);
        String baseType = genericType.substring(0, index);
        List<String> types = new ArrayList<>(StringUtils.split(
                genericType.substring(index + 1, genericType.length() - 1),
                ",",
                Pair.of("<", ">")
        ));
        for (int i = 0; i < types.size(); i++) {
            String type = parseGenericTypesImports(types.get(i));
            addImport(type);
            types.set(i, type.substring(type.lastIndexOf('.') + 1));
        }
        return baseType + String.format(genericsFormat, String.join(", ", types));
    }

    /**
     * Generates a new Java Bean from the given configuration file
     * under the specified source directory and at the given package.
     * <br>
     * If the class already exists, then it is updated with the new configuration
     * entries (or updates).
     *
     * @param configurationFile the configuration file
     * @param sourceDirectory   the src/main/java directory location
     * @param packageName       the package name
     * @param className         the class name
     * @return the newly created bean
     */
    public static @NotNull File generate(final @NotNull File configurationFile,
                                         final @NotNull File sourceDirectory,
                                         final @NotNull String packageName,
                                         final @NotNull String className) throws IOException {
        ConfigurationAdapter configurationAdapter = ConfigurationAdapter.newAdapter(
                log,
                ConfigurationFormat.fromExtension(configurationFile.getName())
        );
        final Map<CommentKey, Object> data = configurationAdapter.loadWithComments(configurationFile);

        final File beanFile = new File(sourceDirectory,
                packageName.replace(".", File.separator) +
                        File.separator + className + ".java"
        );
        Files.createDirectories(beanFile.getParentFile().toPath());

        final CompilationUnit compilationUnit;
        if (beanFile.exists()) compilationUnit = StaticJavaParser.parse(beanFile);
        else compilationUnit = new CompilationUnit();

        compilationUnit.setPackageDeclaration(packageName);

        final Map<String, ImportDeclaration> imports = compilationUnit.getImports().stream()
                .collect(Collectors.toMap(NodeWithName::getNameAsString, i -> i));

        final ClassOrInterfaceDeclaration root = compilationUnit.getClassByName(className)
                .orElseGet(() -> compilationUnit.addClass(className).setPublic(true));

        final BeanConfigurationBuilder builder = new BeanConfigurationBuilder(
                data,
                root,
                imports
        );
        builder.parse();
        builder.imports.values().forEach(compilationUnit::addImport);

        final String code = compilationUnit.toString();
        try (FileOutputStream output = new FileOutputStream(beanFile)) {
            output.write(code.getBytes(StandardCharsets.UTF_8));
        }

        return beanFile;
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
