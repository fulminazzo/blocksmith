package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A loader for {@link Application} instances.
 *
 * @see Application
 * @see ApplicationHandlers
 * @see BlocksmithApplication
 * @see FieldAnnotationHandler
 */
final class ApplicationLoader {
    private final @NotNull Application application;
    private final @NotNull Reflect reflect;

    /**
     * Instantiates a new Application loader.
     *
     * @param application the application
     */
    ApplicationLoader(final @NotNull Application application) {
        this.application = application;
        this.reflect = Reflect.on(application);
    }

    /**
     * Executes all the necessary steps to load the application.
     *
     * @return the root of the dependency tree
     */
    @NotNull LoaderNode load() {
        List<FieldAnnotationNode> nodes = loadFieldAnnotationNodes();
        Map<String, FieldAnnotationNode> namedNodes = toNamedMap(nodes);
        LoaderNode tree = buildDependencyTree(namedNodes);
        validateTree(tree);
        return tree;
    }

    /**
     * Validates the built dependency tree.
     *
     * @param node the root of the tree to validate
     */
    void validateTree(final @NotNull LoaderNode node) {
        checkCircularDependency(node, new LinkedHashSet<>());
    }

    /**
     * Uses the given list of nodes to build a dependency tree.
     *
     * @param nodes the nodes to build the tree from
     * @return the root of the tree
     */
    @NotNull LoaderNode buildDependencyTree(final @NotNull Map<String, FieldAnnotationNode> nodes) {
        final LoaderNode root = new LoaderNode();
        if (nodes.isEmpty()) return root;
        for (FieldAnnotationNode node : nodes.values()) {
            Set<String> fieldDependencies = node.getFieldDependencies();
            if (fieldDependencies.isEmpty()) root.addChild(node);
            else
                for (String dep : fieldDependencies) {
                    FieldAnnotationNode depNode = nodes.get(dep);
                    if (depNode != null) depNode.addChild(node);
                    else throw new InvalidApplicationException(
                            "Invalid dependency declared in application %s and "
                                    + "annotation %s for field '%s': %s not found",
                            application.getClass().getCanonicalName(),
                            node.getAnnotation().annotationType().getCanonicalName(),
                            node.getField().getName(),
                            dep
                    );
                }
        }
        if (root.getChildren().isEmpty())
            throw new InvalidApplicationException(
                    "Circular dependency detected in application %s",
                    application.getClass().getCanonicalName()
            );
        else return root;
    }

    /**
     * Loads all the fields in the {@link #application} that are annotated with an annotation handled by
     * a previously registered {@link FieldAnnotationHandler}.
     *
     * @return the nodes (without their dependencies)
     */
    @NotNull List<FieldAnnotationNode> loadFieldAnnotationNodes() {
        List<FieldAnnotationNode> nodes = new LinkedList<>();
        for (Field field : reflect.getInstanceFields())
            nodes.addAll(loadFieldAnnotationNodeSingle(field));
        return nodes;
    }

    private void checkCircularDependency(
            final @NotNull LoaderNode node,
            final @NotNull Set<LoaderNode> visiting
    ) {
        if (!visiting.add(node))
            throw new InvalidApplicationException(
                    "Circular dependency detected in application %s",
                    application.getClass().getCanonicalName()
            );
        for (LoaderNode child : node.getChildren()) checkCircularDependency(child, visiting);
        visiting.remove(node);
    }

    private @NotNull List<FieldAnnotationNode> loadFieldAnnotationNodeSingle(final @NotNull Field field) {
        List<FieldAnnotationNode> nodes = new LinkedList<>();
        for (Annotation annotation : field.getAnnotations())
            ApplicationHandlers.getFieldAnnotationHandler(annotation.annotationType()).ifPresent(h ->
                    nodes.add(new FieldAnnotationNode(annotation, field, h))
            );
        return nodes;
    }

    /**
     * Converts the given list of nodes to a map where the keys are the field names.
     *
     * @param nodes the nodes to convert
     * @return the map
     */
    static Map<String, FieldAnnotationNode> toNamedMap(final @NotNull List<FieldAnnotationNode> nodes) {
        Map<String, FieldAnnotationNode> namedNodes = new LinkedHashMap<>();
        nodes.forEach(n -> namedNodes.put(n.getFieldName(), n));
        return namedNodes;
    }

    /**
     * Although the name of this function implies it only works for fields, it actually works for getter methods
     * as well. This is how it works:
     * <ul>
     *     <li>Extracts the first part of the {@code fieldPath} preceding any {@code .} symbol.
     *     This will be the target name;</li>
     *     <li>Checks the class in search for a <b>non-static</b> field with the target name;</li>
     *     <li>If it is not found, checks the class in search for a method named {@code get<target-name>};</li>
     *     <li>If it was not found, an exception will be thrown;</li>
     *     <li>If it was found, and the path is not finished (meaning there is more after the {@code .},
     *     the method is called recursively using the target type as class.</li>
     * </ul>
     *
     * @param clazz the class to check
     * @param fieldPath the path to the field
     */
    static void checkFieldInClass(final @NotNull Class<?> clazz, final @NotNull String fieldPath) {
        if (fieldPath.isEmpty()) return;
        String[] split = fieldPath.split("\\.");
        String fieldName = split[0];
        final Class<?> targetClass;

        Reflect reflect = Reflect.on(clazz);
        Optional<Field> fieldOpt = reflect.getInstanceFields().stream()
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findAny();
        if (fieldOpt.isPresent()) targetClass = fieldOpt.get().getType();
        else {
            String methodName = "get" + fieldName;
            Optional<Method> methodOpt = reflect.getInstanceMethods().stream()
                    .filter(m -> m.getName().equalsIgnoreCase(methodName) && m.getParameterCount() == 0)
                    .findAny();
            if (methodOpt.isPresent()) targetClass = methodOpt.get().getReturnType();
            else throw new InvalidApplicationException(
                    "Field '%s' not found in class %s",
                    fieldPath,
                    clazz.getCanonicalName()
            );
        }

        if (split.length > 1)
            checkFieldInClass(
                    targetClass,
                    String.join("", Arrays.copyOfRange(split, 1, split.length))
            );
    }

}
