package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.CriterionUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * An object to validate getters and setters positioning.
 */
public final class GetterSetterValidator extends AbstractValidator<GetterSetterValidator.Scope, GetterSetterValidator> {
    private static final @NotNull String GETTER_PREFIX = "get";
    private static final @NotNull String SETTER_PREFIX = "set";

    @Override
    protected @NotNull Scope newScope() {
        return new Scope();
    }

    @Override
    protected void validateNodeImpl(final @NotNull DetailAST node) throws ValidationException {
        String name = CriterionUtils.getMethodName(node);
        Scope lastScope = getLastScope();
        if (name.startsWith(GETTER_PREFIX)) {
            if (lastScope.getSetters().contains(name.substring(GETTER_PREFIX.length())))
                throw new ValidationException(node, "method.getter");
            else lastScope.registerGetter(name);
        } else if (name.startsWith(SETTER_PREFIX)) {
            lastScope.registerSetter(name);
            String unprefixedName = name.substring(SETTER_PREFIX.length());
            if (unprefixedName.equals(lastScope.getLastGetter())) return;
            if (lastScope.getGetters().contains(unprefixedName))
                throw new ValidationException(node, "method.setter");
        }
    }

    /**
     * Represents a scope for {@link GetterSetterValidator}.
     */
    @Getter
    protected static final class Scope extends AbstractValidator.Scope {
        private final @NotNull LinkedList<String> getters = new LinkedList<>();
        private final @NotNull List<String> setters = new LinkedList<>();

        /**
         * Registers a new getter.
         *
         * @param getter the getter name
         */
        public void registerGetter(final @NotNull String getter) {
            String name = getter.substring(GETTER_PREFIX.length());
            if (!getters.contains(name)) getters.add(name);
        }

        /**
         * Gets the last recorded getter.
         *
         * @return the last getter
         */
        public @Nullable String getLastGetter() {
            return getters.isEmpty() ? null : getters.getLast();
        }

        /**
         * Registers a new setter.
         *
         * @param setter the setter name
         */
        public void registerSetter(final @NotNull String setter) {
            String name = setter.substring(SETTER_PREFIX.length());
            if (!setters.contains(name)) setters.add(name);
        }

    }

}
