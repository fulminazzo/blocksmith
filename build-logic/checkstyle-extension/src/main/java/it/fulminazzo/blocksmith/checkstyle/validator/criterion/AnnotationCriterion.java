package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for annotations.
 *
 * @see Criterion
 */
@RequiredArgsConstructor
public enum AnnotationCriterion implements Criterion {
    /**
     * {@code @Data} criterion.
     */
    DATA("Data"),
    /**
     * {@code @Value} criterion.
     */
    VALUE("Value"),
    /**
     * {@code @Getter} criterion.
     */
    GETTER("Getter"),
    /**
     * {@code @Setter} criterion.
     */
    SETTER("Setter"),
    /**
     * {@code @EqualsAndHashCode} criterion.
     */
    EQUALS_AND_HASH_CODE("EqualsAndHashCode"),
    /**
     * {@code @ToString} criterion.
     */
    TO_STRING("ToString"),
    /**
     * {@code @NoArgsConstructor} criterion.
     */
    NO_ARGS_CONSTRUCTOR("NoArgsConstructor"),
    /**
     * {@code @RequiredArgsConstructor} criterion.
     */
    REQUIRED_ARGS_CONSTRUCTOR("RequiredArgsConstructor"),
    /**
     * {@code @AllArgsConstructor} criterion.
     */
    ALL_ARGS_CONSTRUCTOR("AllArgsConstructor"),
    /**
     * {@code @Builder} criterion.
     */
    BUILDER("Builder");

    private final @NotNull String annotationName;

    @Override
    public boolean matches(final @NotNull DetailAST node) {
        return CriterionUtils.isAnnotationName(node, annotationName);
    }

    @Override
    public @NotNull String getErrorMessage() {
        return "annotation";
    }

    @Override
    public @NotNull String toString() {
        return annotationName;
    }

}
