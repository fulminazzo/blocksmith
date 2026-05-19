package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Abstract implementation of {@link Validator} with common logic.
 *
 * @param <S> the type of the scope
 * @param <A> the type of this validator (for method chaining)
 */
abstract class AbstractValidator<S extends AbstractValidator.Scope, A extends AbstractValidator<S, A>>
        implements Validator {
    private final @NotNull Deque<S> scopes = new ArrayDeque<>();
    private @Nullable Validator next;

    /**
     * Initializes a new scope.
     *
     * @return the scope
     */
    protected abstract @NotNull S newScope();

    /**
     * Contains the actual validation logic.
     *
     * @param node the node to validate
     * @throws ValidationException if the node is not valid
     */
    protected abstract void validateNodeImpl(final @NotNull DetailAST node) throws ValidationException;

    @Override
    public void validateNode(final @NotNull DetailAST node) throws ValidationException {
        getLastScope().register(node);
        validateNodeImpl(node);
    }

    /**
     * Gets the last scope.
     *
     * @return the last scope
     */
    @SuppressWarnings("DataFlowIssue")
    protected @NotNull S getLastScope() {
        if (scopes.isEmpty()) enterScope();
        return scopes.peek();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull A then(final @NotNull Validator validator) {
        if (next != null) next.then(validator);
        else this.next = validator;
        return (A) this;
    }

    @Override
    public void enterScope() {
        scopes.push(newScope());
    }

    @Override
    public void exitScope() throws CompositeValidationException {
        if (!scopes.isEmpty()) {
            S scope = scopes.pop();
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
     * Represents a scope for this class implementations.
     */
    protected abstract static class Scope {
        @Getter
        private final @NotNull List<DetailAST> nodes = new ArrayList<>();

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
