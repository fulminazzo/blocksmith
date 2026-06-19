package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
        LoaderNode tree = buildDependencyTree(nodes);
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
    @NotNull LoaderNode buildDependencyTree(final @NotNull List<FieldAnnotationNode> nodes) {
        final LoaderNode root = new LoaderNode();
        if (nodes.isEmpty()) return root;
        Map<String, FieldAnnotationNode> namedNodes = new HashMap<>();
        nodes.forEach(n -> namedNodes.put(n.getField().getName(), n));
        for (FieldAnnotationNode node : nodes)
            if (node.getDependencies().isEmpty()) root.addChild(node);
            else
                for (String dep : node.getDependencies()) {
                    FieldAnnotationNode depNode = namedNodes.get(dep);
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

}
