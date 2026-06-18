package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Identifies a field with an annotation.
 *
 * @see FieldAnnotationHandler
 * @see ApplicationLoader
 */
@Value
class FieldAnnotationNode {
    private static final @NotNull String DEPENDENCY_FIELD_NAME = "dependsOn";

    @NotNull Annotation annotation;
    @NotNull Field field;
    @NotNull FieldAnnotationHandler<?> handler;
    @EqualsAndHashCode.Exclude
    @NotNull Set<String> dependencies;

    /**
     * Instantiates a new Field annotation node.
     *
     * @param annotation the annotation
     * @param field      the field
     * @param handler    the handler
     */
    public FieldAnnotationNode(
            @NotNull Annotation annotation,
            @NotNull Field field,
            @NotNull FieldAnnotationHandler<?> handler
    ) {
        this.annotation = annotation;
        this.field = field;
        this.handler = handler;

        this.dependencies = Set.copyOf(loadDependencies(this.annotation));
    }

    private static @NotNull Set<String> loadDependencies(final @NotNull Annotation annotation) {
        final Set<String> dependencies = new LinkedHashSet<>();
        Reflect reflect = Reflect.on(annotation);
        reflect.getInstanceFields().stream()
                .filter(f -> f.getName().equals(DEPENDENCY_FIELD_NAME))
                .findAny()
                .ifPresent(f -> {
                    Class<?> fieldType = f.getType();
                    if (String.class.isAssignableFrom(fieldType))
                        addDependency(dependencies, reflect.get(f).get());
                    else if (String[].class.isAssignableFrom(fieldType)) {
                        String[] deps = reflect.get(f).get();
                        for (String dependency : deps)
                            addDependency(dependencies, dependency);
                    } else throw new IllegalArgumentException(
                            "Unsupported dependency type: " + fieldType.getCanonicalName()
                    );
                });
        return dependencies;
    }

    private static void addDependency(
            final @NotNull Set<String> dependencies,
            final @NotNull String dependency
    ) {
        dependencies.add(dependency.split("\\.")[0]);
    }

}
