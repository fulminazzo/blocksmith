package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import it.fulminazzo.blocksmith.validation.annotation.*;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
    private static final @NotNull String propertyFormat = "property '%s'";
    private static final @NotNull String parameterFormat = "parameter at position %s";

    private static final @NotNull Validator INSTANCE = new Validator();

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
     * Validates method invocation parameters.
     * This call should be put first in the method declaration.
     * <br>
     * <pre>{@code
     * public void myMethod(@NonNull String name, @Positive int age) {
     *     Validator.validateMethod(name, age);
     *     // rest of logic
     * }
     * }</pre>
     *
     * @param parameters the actual values of the parameters
     * @throws ViolationException an exception containing all the violations
     */
    public static void validateMethod(final @Nullable Object @NotNull ... parameters) throws ViolationException {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        Method method = Reflect.on(stackTrace.getClassName())
                .getMethod(stackTrace.getMethodName(), Reflect.getParameterTypes(parameters));
        Parameter[] params = method.getParameters();
        if (params.length != parameters.length)
            throw new IllegalArgumentException("Method parameters do not match with the given number of parameters. " +
                    "Please include all the parameters of the method to validate it");
        Map<String, Set<ConstraintViolation>> violations = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            String name = parameter.getName();
            try {
                getInstance().validate(parameter, parameters[i]);
            } catch (ValidationException e) {
                Map<String, Set<ConstraintViolation>> tmp = new HashMap<>(e.getViolations());
                for (String key : new ArrayList<>(tmp.keySet())) {
                    if (key.equals(name)) {
                        Set<ConstraintViolation> value = tmp.get(key);
                        tmp.remove(key, value);
                        tmp.put(String.format(parameterFormat, i), value);
                    }
                }
                violations.putAll(tmp);
            }
        }
        if (!violations.isEmpty())
            throw new ViolationException(violations);
    }

    /**
     * Validates the given object fields against their {@link Constraint} annotations.
     *
     * @param value the value
     * @throws ViolationException an exception containing all the violations
     */
    public static void validate(final @Nullable Object value) throws ViolationException {
        try {
            getInstance().validateBean(value);
        } catch (ValidationException e) {
            throw new ViolationException(e);
        }
    }

    /**
     * Validates the value against the given field.
     * <br>
     * The field must have at least one {@link Constraint} annotated annotation
     * in order for validation to work.
     *
     * @param field the field
     * @param value the value
     * @throws ViolationException an exception containing all the violations
     */
    public static void validateField(final @NotNull Field field, final @Nullable Object value) throws ViolationException {
        try {
            getInstance().validate(field, value);
        } catch (ValidationException e) {
            throw new ViolationException(e);
        }
    }

    /**
     * Validates the given object against the field.
     * Will recursively look up the annotations of the element.
     * Then, will validate the internal fields of the object.
     *
     * @param field the element
     * @param value the value
     * @throws ValidationException if the validation fails
     */
    public void validate(final @NotNull Field field, final @Nullable Object value) throws ValidationException {
        validateRec(field, field.getName(), value);
        validateBean(value);
    }

    /**
     * Validates the given object against the parameter.
     * Will recursively look up the annotations of the element.
     * Then, will validate the internal parameters of the object.
     *
     * @param parameter the element
     * @param value     the value
     * @throws ValidationException if the validation fails
     */
    public void validate(final @NotNull Parameter parameter, final @Nullable Object value) throws ValidationException {
        validateRec(parameter, parameter.getName(), value);
        validateBean(value);
    }

    /**
     * Validates all the fields of the given Java object.
     *
     * @param bean the actual object to validate
     * @throws ValidationException if the validation fails
     */
    public void validateBean(final @Nullable Object bean) throws ValidationException {
        if (bean == null) return;
        final Queue<Object> queue = new LinkedList<>();
        final Map<Object, String> paths = new LinkedHashMap<>();
        final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<String, Set<ConstraintViolation>> violations = new HashMap<>();
        paths.put(bean, "");
        queue.add(bean);
        while (!queue.isEmpty()) {
            final Object current = queue.remove();
            if (current == null || visited.contains(current)) continue;
            final String currentPath = paths.get(current);
            visited.add(current);
            final Reflect beanReflect = Reflect.on(current);
            if (beanReflect.isBaseType()) continue;
            if (current.getClass().getPackageName().startsWith("java")) continue;
            for (Field field : beanReflect.getInstanceFields()) {
                Object value = beanReflect.get(field).get();
                String fieldPath = (currentPath.isEmpty() ? "" : currentPath + ".") + field.getName();
                try {
                    validateRec(field, String.format(propertyFormat, fieldPath), value);
                } catch (ValidationException e) {
                    violations.putAll(e.getViolations());
                }
                queue.add(value);
                paths.put(value, fieldPath);
            }
        }
        if (!violations.isEmpty()) throw new ValidationException(bean, violations);
    }

    private void validateRec(final @NotNull AnnotatedElement annotatedElement,
                             final @NotNull String elementName,
                             final @Nullable Object value) throws ValidationException {
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
        if (!violations.isEmpty())
            throw new ValidationException(String.format(propertyFormat, value), Map.of(elementName, violations));
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
