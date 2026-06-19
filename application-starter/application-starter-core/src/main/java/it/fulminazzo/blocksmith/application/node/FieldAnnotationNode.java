package it.fulminazzo.blocksmith.application.node;

import it.fulminazzo.blocksmith.application.FieldAnnotationHandler;
import it.fulminazzo.blocksmith.application.LoaderVisitor;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Identifies a field with an annotation.
 *
 * @see LoaderNode
 * @see FieldAnnotationHandler
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class FieldAnnotationNode extends LoaderNode {
    private static final @NotNull String DEPENDENCY_FIELD_NAME = "dependsOn";

    @NotNull Annotation annotation;
    @NotNull Field field;
    @NotNull FieldAnnotationHandler<?> handler;
    /**
     * The keys are the fields in the same Application class.
     * The values are the paths of subfields of the field type.
     * If no subfield is specified, the value will be empty.
     */
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @NotNull Map<String, String> dependencies;

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

        this.dependencies = Map.copyOf(loadDependencies(this.annotation));
    }

    /**
     * Gets the path of the subfields of the field with the given name that this node is dependent on.
     * <br>
     * For example, if this node is dependent on the field {@code foo.bar}, the path will be {@code bar}
     * for the field named {@code foo}.
     *
     * @param fieldName the name of the field
     * @return the path of the subfields
     */
    public @NotNull String getFieldDependency(final @NotNull String fieldName) {
        return dependencies.getOrDefault(fieldName, "");
    }

    /**
     * Gets all the fields this node is dependent on.
     *
     * @return the dependencies
     */
    public @NotNull Set<String> getFieldDependencies() {
        return dependencies.keySet();
    }

    /**
     * Gets the name of the field.
     *
     * @return the name
     */
    public @NotNull String getFieldName() {
        return field.getName();
    }

    @Override
    public <X extends Throwable> void accept(final @NotNull LoaderVisitor<X> visitor) throws X {
        visitor.visitField(this);
    }

    private static @NotNull Map<String, String> loadDependencies(final @NotNull Annotation annotation) {
        final Map<String, String> dependencies = new LinkedHashMap<>();
        Reflect reflect = Reflect.on(annotation);
        reflect.getInstanceMethods().stream()
                .filter(m -> m.getName().equals(DEPENDENCY_FIELD_NAME))
                .findAny()
                .ifPresent(m -> {
                    Class<?> returnType = m.getReturnType();
                    if (String.class.isAssignableFrom(returnType))
                        addDependency(dependencies, reflect.invoke(m).get());
                    else if (String[].class.isAssignableFrom(returnType)) {
                        String[] deps = reflect.invoke(m).get();
                        for (String dependency : deps)
                            addDependency(dependencies, dependency);
                    } else throw new IllegalArgumentException(
                            "Unsupported dependency type: " + returnType.getCanonicalName()
                    );
                });
        return dependencies;
    }

    private static void addDependency(
            final @NotNull Map<String, String> dependencies,
            final @NotNull String dependency
    ) {
        String[] split = dependency.split("\\.");
        dependencies.put(
                split[0],
                String.join(".", Arrays.copyOfRange(split, 1, split.length))
        );
    }

}
