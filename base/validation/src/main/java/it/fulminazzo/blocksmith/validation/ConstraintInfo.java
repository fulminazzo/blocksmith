package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents information about a constraint.
 */
@Value
@AllArgsConstructor(access = AccessLevel.NONE)
class ConstraintInfo {
    @Getter(AccessLevel.NONE)
    @Nullable Object value;
    @Nullable String message;
    @NotNull String defaultMessage;

    /**
     * Instantiates a new Constraint info.
     *
     * @param constraint the annotation constraint
     */
    public ConstraintInfo(final @NotNull Annotation constraint) {
        Reflect reflect = Reflect.on(constraint);
        Object value;
        try {
            value = reflect.invoke("value").get();
        } catch (ReflectException e) {
            value = null;
        }
        this.value = value;
        String message;
        try {
            message = reflect.invoke("message").get();
        } catch (ReflectException e) {
            message = null;
        }
        this.message = message;
        this.defaultMessage = Reflect.on(constraint.annotationType())
                .getStatic("DEFAULT_MESSAGE", "Invalid value: %s")
                .get();
    }

    /**
     * Formats the arguments for the error message.
     *
     * @param value the invalid value
     * @return the arguments
     */
    public @NotNull Object @NotNull [] formatArguments(final Object value) {
        List<Object> arguments = new ArrayList<>();
        arguments.add(value);
        if (this.value != null) arguments.add(this.value);
        return arguments.toArray();
    }

}
