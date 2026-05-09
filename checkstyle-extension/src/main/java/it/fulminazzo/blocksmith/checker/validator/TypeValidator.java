package it.fulminazzo.blocksmith.checker.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * {@link RankerValidator} implementation for validating types ordering (interfaces, enums, records and classes).
 *
 * @see RankerValidator
 */
@RequiredArgsConstructor
public enum TypeValidator implements RankerValidator {
    /**
     * {@code interface} validator.
     */
    INTERFACE(TokenTypes.INTERFACE_DEF, "interface"),
    /**
     * {@code enum} validator.
     */
    ENUM(TokenTypes.ENUM_DEF, "enum"),
    /**
     * {@code record} validator.
     */
    RECORD(TokenTypes.RECORD_DEF, "record"),
    /**
     * {@code class} validator.
     */
    CLASS(TokenTypes.CLASS_DEF, "class");

    @Getter
    private final int type;
    private final @NotNull String typeName;

    @Override
    public boolean validate(final @NotNull DetailAST node) {
        return node.getType() == type;
    }

    @Override
    public @NotNull String getErrorMessage() {
        return "order." + typeName;
    }

}
