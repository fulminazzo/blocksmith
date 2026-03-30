package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import it.fulminazzo.blocksmith.validation.annotation.*;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The registry of validators used to validate objects.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Validator {
    private static final Validator INSTANCE = new Validator();

    private final @NotNull Map<Class<? extends Annotation>, Function<? extends Annotation, ConstraintValidator>> validators = new ConcurrentHashMap<>();

    static {
        getInstance()
                .register(NonNull.class, Objects::nonNull)
                .register(AssertFalse.class, o -> o == null || !((Boolean) o))
                .register(AssertTrue.class, o -> o == null || ((Boolean) o))
                .registerSupplier(Max.class, a -> o -> o == null || ((Number) o).doubleValue() <= a.value())
                .register(Negative.class, o -> o == null || ((Number) o).doubleValue() < 0)
                .registerSupplier(Min.class, a -> o -> o == null || ((Number) o).doubleValue() >= a.value())
                .register(Positive.class, o -> o == null || ((Number) o).doubleValue() > 0)
                .registerSupplier(Range.class, a -> o -> o == null || ((Number) o).doubleValue() >= a.min() && ((Number) o).doubleValue() <= a.max())
                .registerSupplier(Size.class, a -> o -> {
                    if (o == null) return true;
                    Reflect reflect = Reflect.on(o);
                    Integer size = reflect.get("length", null).get();
                    if (size == null)
                        try {
                            size = reflect.invoke("length").get();
                        } catch (ReflectException e) {
                            size = reflect.invoke("size").get();
                        }
                    return size != null && size >= a.min() && size <= a.max();
                })
        ;
    }

    /**
     * Validates the given object against the annotated element.
     * Will recursively look up the annotations of the element.
     *
     * @param annotatedElement the element
     * @param value            the value
     * @throws ValidationException if the validation fails
     */
    public void validate(final @NotNull AnnotatedElement annotatedElement, final Object value) throws ValidationException {
        final Map<Class<? extends Annotation>, ConstraintInfo> parents = new HashMap<>();
        final Set<ConstraintViolation> violations = new HashSet<>();
        final Queue<AnnotatedElement> elements = new LinkedList<>();
        elements.add(annotatedElement);
        while (!elements.isEmpty()) {
            AnnotatedElement current = elements.poll();
            for (Annotation annotation : current.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (!annotationType.isAnnotationPresent(Constraint.class)) continue;
                final ConstraintInfo constraintInfo = parents.getOrDefault(annotationType, new ConstraintInfo(annotation));
                Arrays.stream(annotationType.getAnnotations())
                        .map(Annotation::annotationType)
                        .forEach(a -> parents.putIfAbsent(a, constraintInfo));
                final ConstraintValidator validator = getValidator(annotation);
                if (validator != null && !validator.isValid(value))
                    violations.add(ConstraintViolation.of(value, constraintInfo));
                elements.add(annotationType);
            }
        }
        if (!violations.isEmpty()) throw new ValidationException(value, violations);
    }

    /**
     * Gets the validator associated with the given annotation.
     *
     * @param <A>        the annotation type
     * @param annotation the annotation
     * @return the validator (if found)
     */
    public <A extends Annotation> @Nullable ConstraintValidator getValidator(final @NotNull A annotation) {
        Function<A, ConstraintValidator> validatorSupplier = (Function<A, ConstraintValidator>) validators.get(annotation.annotationType());
        if (validatorSupplier == null) return null;
        else return validatorSupplier.apply(annotation);
    }

    /**
     * Registers a new validator for the provided annotation type.
     *
     * @param <A>             the annotation type
     * @param annotationClass the annotation class
     * @param validator       the validator
     * @return this object (for method chaining)
     */
    public <A extends Annotation> @NotNull Validator register(final @NotNull Class<A> annotationClass,
                                                              final @NotNull ConstraintValidator validator) {
        return registerSupplier(annotationClass, a -> validator);
    }

    /**
     * Registers a new validator for the provided annotation type.
     *
     * @param <A>               the annotation type
     * @param annotationClass   the annotation class
     * @param validatorSupplier the function to create a new validator instance from the annotation
     * @return this object (for method chaining)
     */
    public <A extends Annotation> @NotNull Validator registerSupplier(final @NotNull Class<A> annotationClass,
                                                                      final @NotNull Function<A, ConstraintValidator> validatorSupplier) {
        validators.put(annotationClass, validatorSupplier);
        return this;
    }

    /**
     * Gets the validator global instance.
     *
     * @return the instance
     */
    public static @NotNull Validator getInstance() {
        return INSTANCE;
    }

}
