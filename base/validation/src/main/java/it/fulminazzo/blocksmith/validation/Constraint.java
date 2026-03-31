package it.fulminazzo.blocksmith.validation;

import java.lang.annotation.*;

/**
 * Defines a constraint annotation.
 * These annotations are the only ones checked and validated from {@link Validator},
 * so, when creating custom constraints, it is <b>mandatory</b> to annotate it as a constraint.
 * <br>
 * The validator will search for the <code>message()</code> and <code>exceptionMessage()</code>
 * methods to construct a {@link ConstraintViolation}. If those are not given, default values
 * will be used (<code>null</code> for message).
 * <br>
 * Any other non-static method of the annotation will be used as follows:
 * <ul>
 *     <li>if only one method is found (for example <code>value()</code>),
 *     it will be considered the expected value of the constraint,
 *     so it will be put in the arguments map as <code>expected</code>
 *     and will replace the <code>%expected%</code> placeholder in the message;</li>
 *     <li>if only two methods are found (for example <code>min()</code> and <code>max()</code>,
 *     they will be considered as two bound values of the constraint.
 *     As such, they will be put in the arguments map as <code>max</code>
 *     and <code>min</code> and will replace the <code>%max%</code> and <code>%min%</code>
 *     placeholders in the message (the methods will be alphabetically sorted, meaning
 *     the first will be "max" and the second will be "min");</li>
 *     <li>if more than two methods are found, they will be put in the arguments map
 *     (sorted alphabetically) as <code>argument%i%</code> where <code>%i%</code> is
 *     the index of the methods list (starting from 0). Placeholders in the message
 *     will follow the same format (<code>%arg%i%%</code>).</li>
 * </ul>
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
