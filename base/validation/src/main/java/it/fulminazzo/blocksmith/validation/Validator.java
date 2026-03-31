package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import it.fulminazzo.blocksmith.validation.annotation.*;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                .register(NonNull.class, new ConstraintValidatorImpl(Objects::nonNull))
                .register(AssertFalse.class, new BooleanConstraintValidator(o -> o == null || !((Boolean) o)))
                .register(AssertTrue.class, new BooleanConstraintValidator(o -> o == null || ((Boolean) o)))
                .registerSupplier(Max.class, a -> new NumberConstraintValidator(o -> o == null || ((Number) o).doubleValue() <= a.value()))
                .register(Negative.class, new NumberConstraintValidator(o -> o == null || ((Number) o).doubleValue() < 0))
                .registerSupplier(Min.class, a -> new NumberConstraintValidator(o -> o == null || ((Number) o).doubleValue() >= a.value()))
                .register(Positive.class, new NumberConstraintValidator(o -> o == null || ((Number) o).doubleValue() > 0))
                .registerSupplier(Range.class, a -> new NumberConstraintValidator(o -> o == null ||
                        ((Number) o).doubleValue() >= a.min() && ((Number) o).doubleValue() <= a.max())
                )
                .registerSupplier(Size.class, a -> new ConstraintValidatorImpl(o -> {
                    if (o == null) return true;
                    Number size;
                    if (o.getClass().isArray()) size = Array.getLength(o);
                    else {
                        Reflect reflect = Reflect.on(o);
                        try {
                            size = reflect.invoke("length").get();
                        } catch (ReflectException e) {
                            size = reflect.invoke("size").get();
                        }
                    }
                    return size.longValue() >= a.min() && size.longValue() <= a.max();
                }))
                .registerSupplier(Matches.class, a -> new StringConstraintValidator(o -> o == null ||
                        Pattern.compile(a.value()).matcher((CharSequence) o).matches()
                ))
                .register(Url.class, new StringConstraintValidator(o -> {
                    try {
                        if (o != null) new URL(o.toString());
                        return true;
                    } catch (MalformedURLException e) {
                        return false;
                    }
                }))
        ;
    }

    /**
     * Validates all the fields of the given Java object.
     *
     * @param beanType the type of the object
     * @param bean     the actual object to validate
     * @throws ValidationException if the validation fails
     */
    public void validateBean(final @NotNull Class<?> beanType, final @Nullable Object bean) throws ValidationException {
        if (bean == null) return;
        final Reflect beanReflect = Reflect.on(bean);
        for (Field field : Reflect.on(beanType).getInstanceFields())
            validate(field, beanReflect.get(field).get());
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
        final Set<Class<? extends Annotation>> visited = new HashSet<>();
        elements.add(annotatedElement);
        while (!elements.isEmpty()) {
            AnnotatedElement current = elements.poll();
            Set<Annotation> annotations = Arrays.stream(current.getAnnotations())
                    .filter(a -> a.annotationType().isAnnotationPresent(Constraint.class))
                    .collect(Collectors.toSet());
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                final ConstraintValidator validator = getValidator(annotation);
                if (validator != null) {
                    if (!validator.matches(value))
                        violations.add(ConstraintViolation.invalidType(value, validator.getTypeNames()));
                    else if (!validator.isValid(value)) {
                        final ConstraintInfo constraintInfo = parents.getOrDefault(annotationType, new ConstraintInfo(annotation));
                        violations.add(ConstraintViolation.of(value, constraintInfo));
                    }
                }

                if (visited.add(annotationType)) elements.add(annotationType);
            }
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                final ConstraintInfo constraintInfo = parents.getOrDefault(annotationType, new ConstraintInfo(annotation));
                Arrays.stream(annotationType.getAnnotations())
                        .map(Annotation::annotationType)
                        .forEach(a -> parents.putIfAbsent(a, constraintInfo));
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
