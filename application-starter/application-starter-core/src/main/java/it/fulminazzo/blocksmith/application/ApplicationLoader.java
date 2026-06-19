package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode;
import it.fulminazzo.blocksmith.application.node.LoaderNode;
import it.fulminazzo.blocksmith.application.node.RootLoaderNode;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Loads all the necessary data for the given {@link Application} instance.
 * <br>
 * <b>WARNING</b>: loading the application is not enough to consider it properly started.
 * A second process called <b>initialization</b> is required.
 *
 * @see Application
 * @see ApplicationHandlers
 * @see BlocksmithApplication
 * @see FieldAnnotationHandler
 * @see ApplicationInitializer
 */
@Slf4j
final class ApplicationLoader {
    private final @NotNull Application application;
    private final @NotNull Reflect reflect;

    /**
     * Instantiates a new Application loader.
     *
     * @param application the application
     */
    public ApplicationLoader(final @NotNull Application application) {
        this.application = application;
        this.reflect = Reflect.on(application);
    }

    /**
     * Executes all the necessary steps to load the application.
     *
     * @return the root of the dependency tree
     */
    public @NotNull LoaderNode load() {
        Logger logger = application.logger();
        logger.debug("Loading application: {}", getApplicationName());
        List<FieldAnnotationNode> nodes = loadFieldAnnotationNodes();
        logger.debug("Loaded {} nodes", nodes.size());
        Map<String, FieldAnnotationNode> namedNodes = toNamedMap(nodes);
        validateNodesDependencies(namedNodes);
        LoaderNode tree = buildDependencyTree(namedNodes);
        validateTree(tree);
        logger.debug("Successfully built dependency tree");
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
        final LoaderNode root = new RootLoaderNode();
        if (nodes.isEmpty()) return root;
        for (FieldAnnotationNode node : nodes.values()) {
            Set<String> fieldDependencies = node.getFieldDependencies();
            if (fieldDependencies.isEmpty()) root.addChild(node);
            else
                for (String dep : fieldDependencies)
                    nodes.get(dep).addChild(node);
        }
        if (root.getChildren().isEmpty())
            throw loadingException("Circular dependency detected");
        else return root;
    }

    /**
     * Given a map of nodes (whose keys are the field names), this method will validate that all
     * the dependencies declared in the nodes are either present in the map or valid subfields
     * of the field itself.
     *
     * @param nodes the nodes to validate
     */
    void validateNodesDependencies(final Map<String, FieldAnnotationNode> nodes) {
        for (FieldAnnotationNode node : nodes.values()) {
            Set<String> fieldDependencies = node.getFieldDependencies();
            for (String dep : fieldDependencies) {
                FieldAnnotationNode depNode = nodes.get(dep);
                if (depNode != null) {
                    String subfield = node.getFieldDependency(dep);
                    if (!checkFieldInClass(node.getField().getType(), subfield))
                        throw loadingException(node, "Could not find subfield '%s' in field: %s", subfield, dep);
                } else throw loadingException(node, "Could not find field: %s", dep);
            }
        }
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
            throw loadingException("Circular dependency detected");
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

    private @NotNull ApplicationLoadingException loadingException(
            final @NotNull String message,
            final @NotNull Object @NotNull ... args
    ) {
        return new ApplicationLoadingException(
                message + String.format("(application=%s) ", getApplicationName()),
                args
        );
    }

    private @NotNull ApplicationLoadingException loadingException(
            final @NotNull FieldAnnotationNode fieldAnnotationNode,
            final @NotNull String message,
            final @NotNull Object @NotNull ... args
    ) {
        return new ApplicationLoadingException(
                message + String.format(
                        "(application=%s, annotation=%s, field=%s) ",
                        getApplicationName(),
                        fieldAnnotationNode.getAnnotation().annotationType().getCanonicalName(),
                        fieldAnnotationNode.getFieldName()
                ),
                args
        );
    }

    private String getApplicationName() {
        return application.getClass().getCanonicalName();
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
     * @param clazz     the class to check
     * @param fieldPath the path to the field
     * @return {@code true} if the field was found, {@code false} otherwise
     */
    static boolean checkFieldInClass(final @NotNull Class<?> clazz, final @NotNull String fieldPath) {
        if (fieldPath.isEmpty()) return true;
        String[] split = fieldPath.split("\\.");
        String fieldName = split[0];
        Reflect reflect = Reflect.on(clazz);

        final Class<?> targetClass;

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
            else return false;
        }

        return split.length == 1 || checkFieldInClass(
                targetClass,
                String.join(".", Arrays.copyOfRange(split, 1, split.length))
        );
    }

}
