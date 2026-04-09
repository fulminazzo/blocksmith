package it.fulminazzo.blocksmith.config;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithTraversableScope;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.DefaultPrettyPrinterVisitor;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.printer.configuration.imports.IntelliJImportOrderingStrategy;
import it.fulminazzo.blocksmith.structure.Pair;
import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.*;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.javaparser.utils.Utils.isNullOrEmpty;

/**
 * A builder to generate a Java bean from a configuration file.
 */
@Slf4j
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BeanConfigurationBuilder {
    private static final @NotNull PrinterConfiguration printConfiguration = new DefaultPrinterConfiguration()
            .addOption(new DefaultConfigurationOption(
                    DefaultPrinterConfiguration.ConfigOption.SORT_IMPORTS_STRATEGY,
                    new IntelliJImportOrderingStrategy()
            ));

    private static final @NotNull List<Class<? extends Expression>> numberExpressions = Arrays.asList(
            IntegerLiteralExpr.class, LongLiteralExpr.class, DoubleLiteralExpr.class
    );

    private static final @NotNull String defaultJavaPackage = "java.lang";
    private static final @NotNull String genericsFormat = "<%s>";
    private static final @NotNull Class<?> nullClass = Object.class;

    private static final @NotNull String[] lombokGetterAnnotations = Stream.of(
            Getter.class, Data.class, Value.class
    ).map(Class::getSimpleName).toArray(String[]::new);
    private static final @NotNull String[] lombokSetterAnnotations = Stream.of(
            Setter.class, Data.class, Value.class
    ).map(Class::getSimpleName).toArray(String[]::new);

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
     * Parses the <code>version</code> property separately.
     * Only works if a <b>non-null number</b> was specified.
     *
     * @param version the version
     */
    void parseVersion(final @NotNull Number version) {
        final Class<?> versionClass = ConfigVersion.class;
        addImport(versionClass);
        final String propertyName = ConfigVersion.PROPERTY_NAME;
        final Type type = StaticJavaParser.parseType(versionClass.getSimpleName());

        final FieldDeclaration field = fields.computeIfAbsent(
                propertyName,
                k -> root.addPrivateField(type, k).setFinal(true)
        ).setStatic(true);
        if (field.getVariables().isEmpty()) field.addVariable(new VariableDeclarator());

        final double actualVersion = version.doubleValue();

        VariableDeclarator fieldVariable = field.getVariable(0)
                .setName(propertyName)
                .setType(type);
        Expression initializer = fieldVariable.getInitializer().orElse(null);
        if (isValidVersionInitializer(versionClass, initializer)) {
            MethodCallExpr methodCall = getFirstCall(initializer.asMethodCallExpr());
            double current = Double.parseDouble(methodCall.getArgument(0).toString());
            if (current != actualVersion) {
                methodCall.setArgument(0, new DoubleLiteralExpr(actualVersion));
                field.setLineComment("TODO: auto-updated, handle migrations manually");
            }
        } else {
            field.setLineComment("TODO: auto-generated, handle migrations manually");
            fieldVariable.setInitializer(new MethodCallExpr(
                    new NameExpr(versionClass.getSimpleName()),
                    new SimpleName("of"),
                    new NodeList<>(new DoubleLiteralExpr(actualVersion))
            ));
        }
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
                            .setStatic(true)
                            .setFinal(true);
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
        if (key.getKey().equals(ConfigVersion.PROPERTY_NAME) && value instanceof Number)
            parseVersion((Number) value);
        else {
            final String className = getGenericTypeNameFromObject(value);
            generateField(key, className).setInitializer(getInitializer(value));
        }
    }

    private @NotNull VariableDeclarator generateField(final @NotNull CommentKey key,
                                                      final @NotNull String fieldClassName) {
        final String propertyName = key.getKey();
        final Type type = StaticJavaParser.parseType(fieldClassName);

        // field
        final FieldDeclaration field = fields.computeIfAbsent(
                propertyName,
                k -> root.addPrivateField(type, k)
        );
        if (field.getVariables().isEmpty()) field.addVariable(new VariableDeclarator().setName(propertyName));

        // comment annotation
        convertComments(key, field);

        // getter
        if (!isAnnotationPresent(field, lombokGetterAnnotations))
            methods.computeIfAbsent(
                    "get" + capitalize(propertyName),
                    k -> {
                        MethodDeclaration method = root.addMethod(k).setPublic(true);
                        method.createBody().addStatement(new ReturnStmt(new NameExpr(propertyName)));
                        return method;
                    }
            ).setType(fieldClassName).setAbstract(false);

        // setter
        if (!isAnnotationPresent(field, lombokSetterAnnotations)) {
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
        }

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
        } else if (value instanceof String) return String.format("\"%s\"", value.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        );
        else if (value instanceof Character) return String.format(String.format("'%s'", value));
        else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            String initializer = String.format("new %s[]", array.getClass().getComponentType().getSimpleName());
            if (array.length == 0) return initializer.replace("[]", "[0]");
            else return String.format("%s{%s}", initializer, Arrays.stream(array)
                    .map(this::getInitializer)
                    .collect(Collectors.joining(", ")));
        } else return value.toString();
    }

    /**
     * Checks if an annotation with any of the given names is present
     * in the root class or field.
     *
     * @param field       the field declaration
     * @param annotations the annotation names
     * @return <code>true</code> if at least one is
     */
    boolean isAnnotationPresent(final @NotNull FieldDeclaration field,
                                final @NotNull String @NotNull ... annotations) {
        for (String name : annotations) {
            if (root.isAnnotationPresent(name)) return true;
            else if (field.isAnnotationPresent(name)) return true;
        }
        return false;
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
        if (object instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) object;
            String typeName;
            if (collection instanceof List) typeName = List.class.getCanonicalName();
            else if (collection instanceof Set) typeName = Set.class.getCanonicalName();
            else typeName = object.getClass().getCanonicalName();
            typeName += String.format(genericsFormat, guessCollectionGenericType(collection));
            typeName = parseGenericTypesImports(typeName);
            return typeName.substring(typeName.lastIndexOf('.') + 1);
        } else return getTypeFromObject(object).getSimpleName();
    }

    private @NotNull String parseGenericTypesImports(final @NotNull String genericType) {
        int index = genericType.indexOf('<');
        if (index == -1) return genericType.substring(genericType.lastIndexOf('.') + 1);
        String baseType = genericType.substring(0, index);
        addImport(baseType);
        List<String> types = new ArrayList<>(StringUtils.split(
                genericType.substring(index + 1, genericType.length() - 1),
                ",",
                Pair.of("<", ">")
        ));
        for (int i = 0; i < types.size(); i++) {
            String type = parseGenericTypesImports(types.get(i));
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
     * @throws IOException in case of any errors
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
                .orElseGet(() -> compilationUnit.addClass(className).setPublic(true).setFinal(true));

        final BeanConfigurationBuilder builder = new BeanConfigurationBuilder(
                data,
                root,
                imports
        );
        builder.parse();

        builder.imports.values().forEach(compilationUnit::addImport);
        sortClass(root);

        final String code = new DefaultPrettyPrinter(BlocksmithVisitor::new, printConfiguration).print(compilationUnit);

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
        if (value instanceof Float) return Double.class; // Floats suck
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
            } else if (object == null) {
                typesCount.replaceAll((k, v) -> v + 1);
                continue;
            } else typeNames = getBasicTypeNames(object.getClass(), object.getClass());
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

    private static void sortClass(final @NotNull ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.getMembers().sort(Comparator.comparingInt(BeanConfigurationBuilder::getMemberPriority));
        classDeclaration.getMembers().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .forEach(m -> sortClass((ClassOrInterfaceDeclaration) m));
    }

    private static int getMemberPriority(final @NotNull BodyDeclaration<?> member) {
        if (member instanceof FieldDeclaration) return 1;
        if (member instanceof MethodDeclaration) return 2;
        if (member instanceof ClassOrInterfaceDeclaration) return 3;
        return 4;
    }

    private static boolean isValidVersionInitializer(final @NotNull Class<?> classVersion,
                                                     final @Nullable Expression expression) {
        if (!(expression instanceof MethodCallExpr)) return false;
        MethodCallExpr methodCall = (MethodCallExpr) expression;
        methodCall = getFirstCall(methodCall);

        // ConfigVersion.
        Optional<Expression> scopeOpt = methodCall.getScope();
        if (scopeOpt.isEmpty()) return false;
        Expression scope = scopeOpt.get();
        if (!scope.isNameExpr()) return false;
        if (!scope.asNameExpr().getNameAsString().equals(classVersion.getSimpleName())) return false;

        // of
        if (!methodCall.getNameAsString().equals("of")) return false;

        NodeList<Expression> arguments = methodCall.getArguments();
        if (arguments.size() != 1) return false;
        Expression argument = arguments.get(0);
        return numberExpressions.stream().anyMatch(t -> t.isAssignableFrom(argument.getClass()));
    }

    private static @NotNull MethodCallExpr getFirstCall(@NotNull MethodCallExpr expression) {
        Optional<Expression> scope;
        while ((scope = expression.getScope()).isPresent()) {
            Expression scopeExpression = scope.get();
            if (scopeExpression instanceof MethodCallExpr) expression = (MethodCallExpr) scopeExpression;
            else break;
        }
        return expression;
    }

    /**
     * {@link DefaultPrettyPrinterVisitor} override with our custom rules.
     */
    final static class BlocksmithVisitor extends DefaultPrettyPrinterVisitor {

        /**
         * Instantiates a new Blocksmith visitor.
         *
         * @param configuration the configuration
         */
        public BlocksmithVisitor(final @NotNull PrinterConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void visit(final @NotNull ArrayInitializerExpr expression,
                          final @NotNull Void argument) {
            printOrphanCommentsBeforeThisChildNode(expression);
            printComment(expression.getComment(), argument);
            printer.print("{");
            if (!isNullOrEmpty(expression.getValues())) {
                final boolean multiLine = expression.getValues().stream().allMatch(Expression::isStringLiteralExpr);
                if (multiLine) {
                    printer.println();
                    printer.indent();
                    printer.indent();
                } else {
                    printer.print(" ");
                }
                for (final Iterator<Expression> i = expression.getValues().iterator(); i.hasNext(); ) {
                    final Expression expr = i.next();
                    expr.accept(this, argument);
                    if (i.hasNext()) {
                        printer.print(multiLine ? "," : ", ");
                        if (multiLine) printer.println();
                    }
                }
                if (multiLine) {
                    printer.println();
                    printer.unindent();
                    printer.unindent();
                } else {
                    printer.print(" ");
                }
            }
            printOrphanCommentsEnding(expression);
            printer.print("}");
        }

        @Override
        public void visit(final @NotNull MethodCallExpr expression,
                          final @NotNull Void argument) {
            printOrphanCommentsBeforeThisChildNode(expression);
            printComment(expression.getComment(), argument);
            // we are at the last method call of a call chain
            // this means we do not start reindenting for alignment or we undo it
            AtomicBoolean lastMethodInCallChain = new AtomicBoolean(true);
            Node node = expression;
            while (node.getParentNode()
                    .filter(NodeWithTraversableScope.class::isInstance)
                    .map(NodeWithTraversableScope.class::cast)
                    .flatMap(NodeWithTraversableScope::traverseScope)
                    .map(node::equals)
                    .orElse(false)) {
                node = node.getParentNode().orElseThrow(AssertionError::new);
                if (node instanceof MethodCallExpr) {
                    lastMethodInCallChain.set(false);
                    break;
                }
            }
            // search whether there is a method call with scope in the scope already
            // this means that we probably started reindenting for alignment there
            AtomicBoolean methodCallWithScopeInScope = new AtomicBoolean();
            Optional<Expression> s = expression.getScope();
            while (s.filter(NodeWithTraversableScope.class::isInstance).isPresent()) {
                Optional<Expression> parentScope =
                        s.map(NodeWithTraversableScope.class::cast).flatMap(NodeWithTraversableScope::traverseScope);
                if (s.filter(MethodCallExpr.class::isInstance).isPresent() && parentScope.isPresent()) {
                    methodCallWithScopeInScope.set(true);
                    break;
                }
                s = parentScope;
            }
            // we have a scope
            // this means we are not the first method in the chain
            expression.getScope().ifPresent(scope -> {
                scope.accept(this, argument);
                if (methodCallWithScopeInScope.get()) {
                    /* We're a method call on the result of something (method call, property access, ...) that is not stand alone,
                    and not the first one with scope, like:
                    we're x() in a.b().x(), or in a=b().c[15].d.e().x().
                    That means that the "else" has been executed by one of the methods in the scope chain, so that the alignment
                    is set to the "." of that method.
                    That means we will align to that "." when we start a new line: */
                    printer.println();
                } else if (!lastMethodInCallChain.get()) {
                    /* We're the first method call on the result of something in the chain (method call, property access, ...),
                    but we are not at the same time the last method call in that chain, like:
                    we're x() in a().x().y(), or in Long.x().y.z(). That means we get to dictate the indent of following method
                    calls in this chain by setting the cursor to where we are now: just before the "."
                    that start this method call. */
                    printer.indent();
                    printer.indent();
                }
                printer.print(".");
            });
            printTypeArgs(expression, argument);
            expression.getName().accept(this, argument);
            printer.duplicateIndent();
            printArguments(expression.getArguments(), argument);
            printer.unindent();
            if (methodCallWithScopeInScope.get() && lastMethodInCallChain.get()) {
                // undo the aligning after the arguments of the last method call are printed
                printer.unindent();
                printer.unindent();
            }
        }

    }

}
