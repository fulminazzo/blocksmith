package it.fulminazzo.blocksmith.validation;

import java.lang.annotation.*;

/**
 * Defines a constraint annotation.
 * These annotations are the only ones checked and validated from {@link Validator},
 * so, when creating custom constraints, it is <b>mandatory</b> to annotate it as a constraint.
 * <br>
 * The validator will search for the {@code message()} and {@code exceptionMessage()}
 * methods to construct a {@link ConstraintViolation}. If those are not given, default values
 * will be used ({@code null} for the message).
 * <br>
 * Any other non-static method of the annotation will be used to define the values
 * of the constraint which will be available through {@link ConstraintViolation#getArguments()}.
 * Example:
 * <pre>
 * {@code
 * @NonNull // Additional constraint to validate the object with
 * @Constraint
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(ElementType.FIELD)
 * public @interface Names {
 *
 *     String[] value();
 *
 *     String message() default "error.validation.invalid-names";
 *
 *     // Uses Java format notation. The first argument will ALWAYS be the given value.
 *     String exceptionMessage() default "'%1$s' is not allowed. Required one of the following: %2$s";
 *
 * }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Constraint {
}
