package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object to validate executable nodes for their overload ordering.
 *
 * @see Executable
 */
public final class OverloadValidator extends AbstractValidator<OverloadValidator.Scope, OverloadValidator> {

    @Override
    public void validateNodeImpl(final @NotNull DetailAST node) throws ValidationException {
        Executable executable = Executable.of(node);
        Scope lastScope = getLastScope();
        Executable lastExecutable = lastScope.getLastExecutable();
        if (lastExecutable != null && executable.compareTo(lastExecutable) < 0)
            throw new ValidationException(node, "executable.overload");
        lastScope.setLastExecutable(executable);
    }

    @Override
    protected @NotNull Scope newScope() {
        return new Scope();
    }

    /**
     * Represents a scope for {@link OverloadValidator}.
     */
    protected static final class Scope extends AbstractValidator.Scope {
        @Getter
        @Setter
        public @Nullable Executable lastExecutable;

    }

}
