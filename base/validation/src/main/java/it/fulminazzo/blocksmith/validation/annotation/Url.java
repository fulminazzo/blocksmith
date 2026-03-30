package it.fulminazzo.blocksmith.validation.annotation;

import it.fulminazzo.blocksmith.validation.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Identifies a {@link CharSequence} parameter or type that represents a URL.
 * <br>
 * Accepts <code>null</code> values.
 */
@Matches("https?:\\/\\/([\\w\\-]+\\.)+[\\w\\-]+(:\\d{1,5})?(\\/[^\\s]*)?(\\?[^\\s]*)?(#[^\\s]*)?")
@Constraint
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Url {
    /**
     * The default error message to fall back to in case of violation.
     */
    @NotNull String DEFAULT_MESSAGE = "'%1$s' is not a valid URL";

    /**
     * Gets the error message in case of violation.
     * <br>
     * By default, the message is a code that will later be translated by an appropriate translator.
     *
     * @return the message
     */
    @NotNull String message() default "error.validation.invalid-url";

}
