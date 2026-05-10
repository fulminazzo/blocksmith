package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Criterion} implementation for order of declaration.
 *
 * @see Criterion
 */
public enum DeclarationCriterion implements Criterion {
    /**
     * Fields criterion.
     */
    FIELD(TokenTypes.VARIABLE_DEF) {
        @Override
        public boolean matches(final @NotNull DetailAST node) {
            return super.matches(node) && node.getParent().getType() == TokenTypes.OBJBLOCK;
        }
    },
    /**
     * Constructors criterion.
     */
    CONSTRUCTOR(TokenTypes.CTOR_DEF),
    /**
     * Methods criterion.
     */
    METHOD(TokenTypes.METHOD_DEF),
    /**
     * Types criterion.
     */
    TYPE(Arrays.stream(TypeCriterion.values()).map(TypeCriterion::getType).toArray(Integer[]::new));

    @Getter
    private final @NotNull List<Integer> types;

    DeclarationCriterion(final @NotNull Integer @NotNull ... types) {
        this.types = Arrays.asList(types);
    }

    @Override
    public boolean matches(final @NotNull DetailAST node) {
        return types.contains(node.getType());
    }

    @Override
    public @NotNull String getErrorMessage() {
        return "declaration";
    }

}
