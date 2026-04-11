package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link ConstraintValidator}.
 */
class ConstraintValidatorImpl implements ConstraintValidator {
    private final @NotNull Type[] types;
    private final @NotNull Predicate<Object> validPredicate;

    /**
     * Instantiates a new Constraint validator.
     *
     * @param validPredicate the valid predicate
     * @param types          the types
     */
    public ConstraintValidatorImpl(final @NotNull Predicate<Object> validPredicate,
                                   final @NotNull Type @NotNull ... types) {
        this.validPredicate = validPredicate;
        this.types = types;
    }

    @Override
    public boolean matches(final Object value) {
        if (value == null || types.length == 0) return true;
        Reflect reflect = Reflect.on(value);
        for (Type type : types)
            if (reflect.extendsType(type)) return true;
        return false;
    }

    @Override
    public @NotNull String getTypeNames() {
        return Arrays.stream(types).map(ReflectUtils::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValid(final Object value) {
        return validPredicate.test(value);
    }


}
