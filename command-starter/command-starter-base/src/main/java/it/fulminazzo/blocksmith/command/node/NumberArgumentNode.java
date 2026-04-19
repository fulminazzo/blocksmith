package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.validation.ValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Special implementation of {@link ArgumentNode} for {@link Number} arguments.
 *
 * @param <N> the type of the argument
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class NumberArgumentNode<N extends Number> extends ArgumentNode<N> {
    double min;
    double max;

    /**
     * Instantiates a new Number argument node.
     *
     * @param name      the name of the node
     * @param parameter the parameter that corresponds to the node
     * @param optional  {@code true} if the node may be omitted
     */
    NumberArgumentNode(final @NotNull String name, final @NotNull Parameter parameter, final boolean optional) {
        super(name, parameter, optional);
        Class<N> type = getType();
        if (type.equals(Byte.class)) {
            min = Byte.MIN_VALUE;
            max = Byte.MAX_VALUE;
        } else if (type.equals(Short.class)) {
            min = Short.MIN_VALUE;
            max = Short.MAX_VALUE;
        } else if (type.equals(Integer.class)) {
            min = Integer.MIN_VALUE;
            max = Integer.MAX_VALUE;
        } else if (type.equals(Long.class)) {
            min = Long.MIN_VALUE;
            max = Long.MAX_VALUE;
        } else if (type.equals(Float.class)) {
            min = -Float.MAX_VALUE;
            max = Float.MAX_VALUE;
        } else {
            min = -Double.MAX_VALUE;
            max = Double.MAX_VALUE;
        }
    }

    /**
     * Sets the minimum allowed value.
     *
     * @param min the value
     * @return this object (for method chaining)
     */
    public @NotNull NumberArgumentNode<N> min(final double min) {
        this.min = min;
        return this;
    }

    /**
     * Sets the minimum allowed value.
     *
     * @param max the value
     * @return this object (for method chaining)
     */
    public @NotNull NumberArgumentNode<N> max(final double max) {
        this.max = max;
        return this;
    }

    @Override
    public @Nullable N parseCurrent(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException, ValidationException {
        N number = super.parseCurrent(visitor);
        if (number == null) return null;
        double value = number.doubleValue();
        if (value < min || value > max)
            throw new ArgumentParseException("error.invalid-number")
                    .arguments(
                            Placeholder.of("argument", visitor.getInput().getCurrent()),
                            Placeholder.of("min", Reflect.on(min).cast(getType())),
                            Placeholder.of("max", Reflect.on(max).cast(getType()))
                    );
        return number;
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        List<String> completions = super.getCompletions(visitor);
        completions.removeIf(s -> {
            try {
                double value = Double.parseDouble(s);
                return value < min || value > max;
            } catch (NumberFormatException e) {
                /*
                 * this is a rare case, but if the completions supply some special kind of number,
                 * which is not a double but the user provided a custom parser for it,
                 * we should allow it to exist.
                 *
                 * For example, imagine a custom Argument Parser for the constant "PI".
                 * When typing "PI" this would allow the option to exist.
                 */
                return false;
            }
        });
        return completions;
    }

}
