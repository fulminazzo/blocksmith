package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * Represents information about a constraint.
 */
@Value
@AllArgsConstructor(access = AccessLevel.NONE)
class ConstraintInfo {
    @Nullable String message;
    @NotNull String defaultMessage;

    /**
     * Instantiates a new Constraint info.
     *
     * @param constraint the annotation constraint
     */
    public ConstraintInfo(final @NotNull Annotation constraint) {
        String message;
        try {
            message = Reflect.on(constraint).invoke("message").get();
        } catch (ReflectException e) {
            message = null;
        }
        this.message = message;
        this.defaultMessage = Reflect.on(constraint.annotationType())
                .getStatic("DEFAULT_MESSAGE", "Invalid value: %s")
                .get();
    }

}
