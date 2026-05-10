package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.CriterionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * An object to enforce pairing of methods.
 * For example, will force the grouping of getters and setters in said order.
 */
@RequiredArgsConstructor
public final class PairedMethodsValidator
        extends AbstractValidator<PairedMethodsValidator.Scope, PairedMethodsValidator> {
    public static final @NotNull List<String> GETTERS = List.of("get", "is");
    public static final @NotNull List<String> SETTERS = List.of("set");

    private final @NotNull List<String> firstPrefixes;
    private final @NotNull List<String> secondPrefixes;

    @Override
    protected @NotNull Scope newScope() {
        return new Scope();
    }

    @Override
    protected void validateNodeImpl(final @NotNull DetailAST node) throws ValidationException {
        String name = CriterionUtils.getMethodName(node);
        Scope lastScope = getLastScope();
        for (String prefix : firstPrefixes)
            if (name.startsWith(prefix)) {
                String unprefixedName = name.substring(prefix.length());
                if (lastScope.getSecond().contains(unprefixedName))
                    throw new ValidationException(node, "method.pair.first")
                            .addArgument(prefix)
                            .addArgument(String.join(", ", secondPrefixes));
                else lastScope.registerFirst(unprefixedName);
            }

        for (String prefix : secondPrefixes)
            if (name.startsWith(prefix)) {
                String unprefixedName = name.substring(prefix.length());
                lastScope.registerSecond(unprefixedName);
                if (unprefixedName.equals(lastScope.getLastFirst())) return;
                if (lastScope.getFirst().contains(unprefixedName))
                    throw new ValidationException(node, "method.pair.second")
                            .addArgument(prefix)
                            .addArgument(String.join(", ", firstPrefixes));
            }
    }

    /**
     * Represents a scope for {@link PairedMethodsValidator}.
     */
    @Getter
    protected static final class Scope extends AbstractValidator.Scope {
        private final @NotNull LinkedList<String> first = new LinkedList<>();
        private final @NotNull List<String> second = new LinkedList<>();

        /**
         * Registers a new first.
         *
         * @param name the name of the first
         */
        public void registerFirst(final @NotNull String name) {
            if (!first.contains(name)) first.add(name);
        }

        /**
         * Gets the last recorded first.
         *
         * @return the last first
         */
        public @Nullable String getLastFirst() {
            return first.isEmpty() ? null : first.getLast();
        }

        /**
         * Registers a new second.
         *
         * @param name the name of the second
         */
        public void registerSecond(final @NotNull String name) {
            if (!second.contains(name)) second.add(name);
        }

    }

}
