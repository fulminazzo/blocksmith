package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode;
import it.fulminazzo.blocksmith.application.node.LoaderNode;
import it.fulminazzo.blocksmith.application.node.RootLoaderNode;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
final class ApplicationInitializer implements LoaderVisitor {
    private final @NotNull Map<@NotNull String, Object> environment = new HashMap<>();

    private final @NotNull Application application;
    private final @NotNull Reflect reflect;

    private final @NotNull RootLoaderNode rootLoaderNode;

    /**
     * Instantiates a new Application initializer.
     *
     * @param application the application
     */
    public ApplicationInitializer(
            final @NotNull Application application,
            final @NotNull RootLoaderNode rootLoaderNode
    ) {
        this.application = application;
        this.reflect = Reflect.on(application);

        this.rootLoaderNode = rootLoaderNode;
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> void visitFieldImpl(final @NotNull FieldAnnotationNode node) {
        final A annotation = (A) node.getAnnotation();
        final Field field = node.getField();
        final FieldAnnotationHandler<A> handler = (FieldAnnotationHandler<A>) node.getHandler();
        // TODO: environment should have dependencies explicitly declared
        //   for example: config.databaseConfig
        Map<@NotNull String, Object> env = new HashMap<>(environment);
        Object result = handler.handle(application, annotation, field, env);
        if (result != null) {
            environment.put(field.getName(), result);
            reflect.set(field, result);
        }
    }

    @Override
    public void visitField(final @NotNull FieldAnnotationNode node) {
        visitFieldImpl(node);
    }

    @Override
    public void visitRoot(final @NotNull RootLoaderNode node) {
        environment.clear();
        final Set<LoaderNode> current = new HashSet<>();
        final Set<LoaderNode> next = new HashSet<>();
        current.add(node);
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

}
