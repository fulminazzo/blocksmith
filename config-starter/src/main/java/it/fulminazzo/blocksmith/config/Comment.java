package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Any annotated field will be serialized with a prefixed
 * comment with {@link #value()} as text.
 * <br>
 * Only for data formats languages that supports comments (such as YAML).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Comment {

    /**
     * The text of the comment.
     * If on JDK < 15, it is possible to specify new lines as
     * elements of the array (or using the newline character <code>\n</code>).
     *
     * @return the text
     */
    @NotNull String @NotNull [] value();

}
