package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An object to validate executable nodes for their overload ordering.
 *
 * @see Executable
 */
public final class OverloadValidator implements Validator {
    private final @NotNull Deque<Scope> scopes = new ArrayDeque<>();
    private @Nullable Validator next;

    @SuppressWarnings("DataFlowIssue")
    private @NotNull Scope getLastScope() {
        if (scopes.isEmpty()) enterScope();
        return scopes.peek();
    }

    @Override
    public void validateNode(final @NotNull DetailAST node) throws ValidationException {
        Executable executable = Executable.of(node);
        Scope lastScope = getLastScope();
        Executable lastExecutable = lastScope.getLastExecutable();
        if (lastExecutable != null && executable.compareTo(lastExecutable) < 0)
            throw new ValidationException(node, "overload");
        lastScope.setLastExecutable(executable);
        lastScope.register(node);
    }

    @Override
    public @NotNull OverloadValidator then(final @NotNull Validator validator) {
        this.next = validator;
        return this;
    }

    @Override
    public void enterScope() {
        scopes.push(new Scope());
    }

    @Override
    public void exitScope() throws CompositeValidationException {
        if (!scopes.isEmpty()) {
            Scope scope = scopes.pop();
            if (next == null) return;
            next.enterScope();
            List<ValidationException> exceptions = new ArrayList<>();
            for (DetailAST node : scope.getNodes()) {
                try {
                    next.validateNode(node);
                } catch (ValidationException e) {
                    exceptions.add(e);
                }
            }
            next.exitScope();
            if (!exceptions.isEmpty()) throw new CompositeValidationException(exceptions);
        }
    }

    /**
     * Represents a scope for {@link OverloadValidator}.
     */
    private static final class Scope {
        @Getter
        private final @NotNull List<DetailAST> nodes = new ArrayList<>();
        @Getter
        @Setter
        public @Nullable Executable lastExecutable;

        /**
         * Registers a node.
         *
         * @param node the node
         */
        public void register(final @NotNull DetailAST node) {
            nodes.add(node);
        }

    }

}
