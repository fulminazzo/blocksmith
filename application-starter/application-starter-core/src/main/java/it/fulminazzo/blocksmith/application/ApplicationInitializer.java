package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode;
import it.fulminazzo.blocksmith.application.node.LoaderNode;
import it.fulminazzo.blocksmith.application.node.RootLoaderNode;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Initializes the given {@link Application} instance.
 * <br>
 * <b>WARNING</b>: requires a {@link RootLoaderNode} dependency tree,
 * previously created in the <b>loading</b> phase.
 *
 * @see Application
 * @see BlocksmithApplication
 * @see ApplicationLoader
 */
final class ApplicationInitializer implements LoaderVisitor<ApplicationEnableException> {
    private final @NotNull Map<@NotNull String, Object> environment = new HashMap<>();

    private final @NotNull Application application;
    private final @NotNull Reflect reflect;

    private final @NotNull LoaderNode dependencyTree;

    /**
     * Instantiates a new Application initializer.
     *
     * @param application    the application
     * @param dependencyTree the dependency tree
     */
    public ApplicationInitializer(
            final @NotNull Application application,
            final @NotNull LoaderNode dependencyTree
    ) {
        this.application = application;
        this.reflect = Reflect.on(application);

        this.dependencyTree = dependencyTree;
    }

    /**
     * Initializes the application.
     *
     * @throws ApplicationEnableException if an error occurs during the initialization
     */
    public void initialize() throws ApplicationEnableException {
        dependencyTree.accept(this);
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> void visitFieldImpl(
            final @NotNull FieldAnnotationNode node
    ) throws ApplicationEnableException {
        final A annotation = (A) node.getAnnotation();
        final Field field = node.getField();
        final FieldAnnotationHandler<A> handler = (FieldAnnotationHandler<A>) node.getHandler();

        for (String dep : node.getFieldDependencies()) {
            String subfield = node.getFieldDependency(dep);
            String fieldPath = dep + (subfield.isEmpty() ? "" : "." + subfield);
            environment.computeIfAbsent(fieldPath, k -> getFieldValue(application, k));
        }

        Object result = handler.handle(application, annotation, field, Map.copyOf(environment));
        if (result != null) {
            environment.put(field.getName(), result);
            reflect.set(field, result);
        }
    }

    @Override
    public void visitField(final @NotNull FieldAnnotationNode node) throws ApplicationEnableException {
        visitFieldImpl(node);
    }

    @Override
    public void visitRoot(final @NotNull RootLoaderNode node) throws ApplicationEnableException {
        environment.clear();
        final Set<LoaderNode> current = new HashSet<>(node.getChildren());
        final Set<LoaderNode> next = new HashSet<>();
        while (!current.isEmpty()) {
            for (LoaderNode child : current) {
                child.accept(this);
                next.addAll(child.getChildren());
            }
            current.clear();
            current.addAll(next);
            next.clear();
        }
    }

    /**
     * Although the name of this function implies it only works for fields, it actually works for getter methods
     * as well. This is how it works:
     * <ul>
     *     <li>Extracts the first part of the {@code fieldPath} preceding any {@code .} symbol.
     *     This will be the target name;</li>
     *     <li>Checks the object class in search for a <b>non-static</b> field with the target name;</li>
     *     <li>If it is not found, checks the object class in search for a method named {@code get<target-name>};</li>
     *     <li>If it was not found, an exception will be thrown;</li>
     *     <li>If it was found, and the path is not finished (meaning there is more after the {@code .},
     *     the method is called recursively using the result as object.</li>
     *     <li>If the result was {@code null},
     *     then the search will be interrupted and that will be returned instead.</li>
     * </ul>
     *
     * @param object    the object
     * @param fieldPath the field path
     * @return the field value
     */
    static @Nullable Object getFieldValue(final @NotNull Object object, final @NotNull String fieldPath) {
        if (fieldPath.isEmpty()) return object;
        String[] split = fieldPath.split("\\.");
        String fieldName = split[0];
        Reflect reflect = Reflect.on(object);

        final Object target;

        Optional<Field> fieldOpt = reflect.getInstanceFields().stream()
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findAny();
        if (fieldOpt.isPresent()) target = reflect.get(fieldOpt.get()).get();
        else {
            String methodName = "get" + fieldName;
            Optional<Method> methodOpt = reflect.getInstanceMethods().stream()
                    .filter(m -> m.getName().equalsIgnoreCase(methodName) && m.getParameterCount() == 0)
                    .findAny();
            if (methodOpt.isPresent()) target = reflect.invoke(methodOpt.get()).get();
            else
                // should never happen in a properly load-initialize cycle
                throw new IllegalArgumentException("Field not found: " + fieldName);
        }

        if (split.length == 1) return target;
        else return getFieldValue(
                target,
                String.join(".", Arrays.copyOfRange(split, 1, split.length))
        );
    }

}
