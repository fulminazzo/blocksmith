package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A loader for {@link Application} instances.
 *
 * @see Application
 * @see BlocksmithApplication
 * @see FieldAnnotationHandler
 */
public final class ApplicationLoader {
    private static final @NotNull Map<
            Class<? extends Annotation>,
            FieldAnnotationHandler<?>
            > FIELD_ANNOTATION_HANDLERS = new ConcurrentHashMap<>();

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
        LoaderNode tree = buildDependencyTree(nodes);
        validateTree(tree);
        return tree;
    }

    /**
     * Validates the built dependency tree by checking if any circular dependency is present.
     *
     * @param node the root of the tree to validate
     */
    void validateTree(final @NotNull LoaderNode node) {
        int occurrences = validateNode(node, node);
        if (occurrences > 1) throw new InvalidApplicationException(
                "Circular dependency detected in application %s",
                application.getClass().getCanonicalName()
        );
        for (LoaderNode child : node.getChildren())
            validateTree(child);
    }

    private int validateNode(final @NotNull LoaderNode node, final @NotNull LoaderNode target) {
        int occurrences = 0;
        for (LoaderNode child : node.getChildren())
            if (child.equals(target)) occurrences++;
        for (LoaderNode child : node.getChildren()) {
            occurrences += validateNode(child, target);
            if (occurrences > 1) return occurrences;
        }
        return occurrences;
    }

    /**
     * Uses the given list of nodes to build a dependency tree.
     *
     * @param nodes the nodes to build the tree from
     * @return the root of the tree
     */
    @NotNull LoaderNode buildDependencyTree(final @NotNull List<FieldAnnotationNode> nodes) {
        final LoaderNode root = new LoaderNode();
        if (nodes.isEmpty()) return root;
        Map<String, FieldAnnotationNode> namedNodes = new HashMap<>();
        nodes.forEach(n -> namedNodes.put(n.getField().getName(), n));
        for (FieldAnnotationNode node : nodes)
            if (node.getDependencies().isEmpty()) root.addChild(node);
            else for (String dep : node.getDependencies()) {
                FieldAnnotationNode depNode = namedNodes.get(dep);
                if (depNode != null) depNode.addChild(node);
                else throw new InvalidApplicationException(
                        "Invalid dependency declared in application %s and annotation %s for field '%s': %s not found",
                        application.getClass().getCanonicalName(),
                        node.getAnnotation().annotationType().getCanonicalName(),
                        node.getField().getName(),
                        dep
                );
            }
        if (root.getChildren().isEmpty())
            throw new InvalidApplicationException(
                    "Detected circular dependencies in application %s",
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

    private @NotNull List<FieldAnnotationNode> loadFieldAnnotationNodeSingle(final @NotNull Field field) {
        List<FieldAnnotationNode> nodes = new LinkedList<>();
        for (Annotation annotation : field.getAnnotations())
            getFieldAnnotationHandler(annotation.annotationType()).ifPresent(h ->
                    nodes.add(new FieldAnnotationNode(annotation, field, h))
            );
        return nodes;
    }

    /**
     * Register a new field annotation handler.
     *
     * @param <A>             the type of the annotation
     * @param annotationClass the annotation class to handle
     * @param handler         the handler to use
     */
    public static <A extends Annotation> void registerFieldAnnotationHandler(
            final @NotNull Class<A> annotationClass,
            final @NotNull FieldAnnotationHandler<A> handler
    ) {
        FIELD_ANNOTATION_HANDLERS.put(annotationClass, handler);
    }

    /**
     * Gets the field annotation handler for the given annotation class.
     *
     * @param annotationClass the annotation class
     * @return the field annotation handler (if found)
     */
    static @NotNull Optional<FieldAnnotationHandler<?>> getFieldAnnotationHandler(
            final @NotNull Class<? extends Annotation> annotationClass
    ) {
        return Optional.ofNullable(FIELD_ANNOTATION_HANDLERS.get(annotationClass));
    }

}
