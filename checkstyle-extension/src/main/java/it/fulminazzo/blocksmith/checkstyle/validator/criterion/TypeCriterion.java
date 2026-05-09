package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Criterion} implementation for types (interfaces, enums, records and classes).
 *
 * @see Criterion
 */
@RequiredArgsConstructor
public enum TypeCriterion implements Criterion {
    /**
     * {@code interface} criterion.
     */
    INTERFACE(TokenTypes.INTERFACE_DEF, "interface"),
    /**
     * {@code enum} criterion.
     */
    ENUM(TokenTypes.ENUM_DEF, "enum"),
    /**
     * {@code record} criterion.
     */
    RECORD(TokenTypes.RECORD_DEF, "record"),
    /**
     * {@code class} criterion.
     */
    CLASS(TokenTypes.CLASS_DEF, "class");

    @Getter
    private final int type;
    private final @NotNull String typeName;

    @Override
    public boolean matches(final @NotNull DetailAST node) {
        return node.getType() == type;
    }

    @Override
    public @NotNull String getErrorMessage() {
        return typeName;
    }

}
